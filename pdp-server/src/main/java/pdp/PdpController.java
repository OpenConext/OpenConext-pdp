package pdp;

import org.apache.openaz.xacml.api.*;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.std.StdMutableRequest;
import org.apache.openaz.xacml.std.StdRequest;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.xacml.PDPEngineHolder;
import pdp.xacml.PdpParseException;
import pdp.xacml.PdpPolicyDefinitionParser;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.openaz.xacml.api.Decision.DENY;
import static org.apache.openaz.xacml.api.Decision.INDETERMINATE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static pdp.PdpApplication.singletonOptionalCollector;
import static pdp.repositories.PdpPolicyViolationRepository.NO_POLICY_ID;

@RestController
@RequestMapping(produces = {"application/json"})
public class PdpController {

  private static Logger LOG = LoggerFactory.getLogger(PdpController.class);
  private final PDPEngineHolder pdpEngineHolder;
  private final PdpPolicyViolationRepository pdpPolicyViolationRepository;
  private final PdpPolicyRepository pdpPolicyRepository;
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final PolicyTemplateEngine policyTemplateEngine = new PolicyTemplateEngine();
  private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();

  private PDPEngine pdpEngine;

  @Autowired
  public PdpController(@Value("${initial.delay.policies.refresh.minutes}") int initialDelay,
                       @Value("${period.policies.refresh.minutes}") int period,
                       PdpPolicyViolationRepository pdpPolicyViolationRepository,
                       PdpPolicyRepository pdpPolicyRepository,
                       PDPEngineHolder pdpEngineHolder) {
    this.pdpEngineHolder = pdpEngineHolder;
    this.pdpEngine = pdpEngineHolder.newPdpEngine();
    this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
    this.pdpPolicyRepository = pdpPolicyRepository;

    newScheduledThreadPool(1).scheduleAtFixedRate(() ->
        this.refreshPolicies(), initialDelay, period, TimeUnit.MINUTES);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/decide/policy")
  public String decide(@RequestBody String payload) throws Exception {
    long start = System.currentTimeMillis();
    LOG.debug("decide request: {}", payload);

    Request request = JSONRequest.load(payload);

    if (!request.getReturnPolicyIdList()) {
      request = createReturnPolicyIdListRequest(request);
    }

    Response pdpResponse;
    try {
      lock.readLock().lock();
      pdpResponse = pdpEngine.decide(request);
    } finally {
      lock.readLock().unlock();
    }
    String response = JSONResponse.toString(pdpResponse, LOG.isDebugEnabled());
    LOG.debug("decide response: {} took: {} ms", response, System.currentTimeMillis() - start);

    reportPolicyViolation(pdpResponse, response, payload);
    return response;
  }

  private Request createReturnPolicyIdListRequest(Request originalRequest) {
    StdMutableRequest request = new StdMutableRequest();
    originalRequest.getRequestAttributes().stream().forEach(reqAttr -> request.add(reqAttr));
    request.setReturnPolicyIdList(true);
    return new StdRequest(originalRequest);
  }

  @RequestMapping(method = GET, value = "/internal/policies")
  public List<PdpPolicyDefinition> policyDefinitions() throws DOMStructureException {
    Iterable<PdpPolicy> all = pdpPolicyRepository.findAll();
    List<PdpPolicyDefinition> policies = stream(all.spliterator(), false).map(policy -> pdpPolicyDefinitionParser.parse(policy)).collect(toList());
    return policies;
  }

  @RequestMapping(method = GET, value = "/internal/policies/{id}")
  public PdpPolicyDefinition policyDefinition(@PathVariable Long id) throws DOMStructureException {
    PdpPolicy policy = pdpPolicyRepository.findOne(id);
    //TODO ensure the violations are also in there
    return pdpPolicyDefinitionParser.parse(policy);
  }

  @RequestMapping(method = POST, value = "/internal/policies")
  public List<PdpPolicyDefinition> post(@RequestBody @Valid PdpPolicyDefinition policyDefintion) {
    String policyXml = policyTemplateEngine.createPolicyXml(policyDefintion);
    try {
      DOMPolicyDef.load(new ByteArrayInputStream(policyXml.replaceFirst("\n", "").getBytes()));
      pdpPolicyRepository.save(new PdpPolicy(policyXml, policyDefintion.getName()));
      return policyDefinitions();
    } catch (DOMStructureException e) {
      throw new PdpPolicyException("policyXml", e.getMessage());
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("pdp_policy_name_unique")) {
        throw new PdpPolicyException("name", "Policy name must be unique. " + policyDefintion.getName() + " is already taken");
      } else {
        throw e;
      }
    }
  }

  @RequestMapping(method = GET, value = "internal/users/me")
  public Object user() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication;
  }

  private void reportPolicyViolation(Response pdpResponse, String response, String payload) {
    Collection<Result> results = pdpResponse.getResults();
    List<Result> deniesOrIndeterminates = results.stream().filter(result ->
        result.getDecision().equals(DENY) || result.getDecision().equals(INDETERMINATE)).collect(toList());
    if (!CollectionUtils.isEmpty(deniesOrIndeterminates)) {
      String policyId = getPolicyId(deniesOrIndeterminates);
      pdpPolicyViolationRepository.save(new PdpPolicyViolation(policyId, payload, response));
    }
  }

  private String getPolicyId(List<Result> deniesOrIndeterminates) {
    Result result = deniesOrIndeterminates.get(0);
    Collection<IdReference> policyIdentifiers = result.getPolicyIdentifiers();
    Collection<IdReference> policySetIdentifiers = result.getPolicySetIdentifiers();
    Optional<IdReference> idReference = !CollectionUtils.isEmpty(policyIdentifiers) ?
        policyIdentifiers.stream().collect(singletonOptionalCollector()) :
        policySetIdentifiers.stream().collect(singletonOptionalCollector());
    return idReference.isPresent() ? idReference.get().getId().stringValue() : NO_POLICY_ID;
  }

  private void refreshPolicies() {
    LOG.info("Starting reloading policies");
    long start = System.currentTimeMillis();
    lock.writeLock().lock();
    try {
      this.pdpEngine = pdpEngineHolder.newPdpEngine();
    } finally {
      lock.writeLock().unlock();
    }
    LOG.info("Finished reloading policies in {} ms", System.currentTimeMillis() - start);
  }

}
