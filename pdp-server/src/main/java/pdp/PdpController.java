package pdp;

import org.apache.openaz.xacml.api.Advice;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.policy.PolicyDef;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.xacml.PDPEngineHolder;
import pdp.xacml.PdpPolicyDefinitionParser;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.openaz.xacml.api.Decision.DENY;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable task = () -> this.refreshPolicies();
    executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MINUTES);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/decide/policy")
  public String decide(@RequestBody String payload) throws Exception {
    long start = System.currentTimeMillis();
    LOG.debug("decide request: {}", payload);

    Request pdpRequest = JSONRequest.load(payload);

    Response pdpResponse;
    try {
      lock.readLock().lock();
      pdpResponse = pdpEngine.decide(pdpRequest);
    } finally {
      lock.readLock().unlock();
    }
    String response = JSONResponse.toString(pdpResponse, LOG.isDebugEnabled());
    LOG.debug("decide response: {} took: {} ms", response, System.currentTimeMillis() - start);

    reportPolicyViolation(pdpResponse, payload);
    return response;
  }

  @RequestMapping(method = GET, value = "/internal/policies")
  public List<PdpPolicyDefinition> policyDefinitions() throws DOMStructureException {
    Iterable<PdpPolicy> all = pdpPolicyRepository.findAll();
    List<PdpPolicyDefinition> policies = stream(all.spliterator(), false).map(policy -> pdpPolicyDefinitionParser.parse(policy.getName(), policy.getPolicyXml())).collect(toList());
    return policies;
  }

  @RequestMapping(method = POST, value = "/internal/policies")
  public List<PdpPolicyDefinition> post(@RequestBody @Valid PdpPolicyDefinition policyDefintion)  {
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

  private void reportPolicyViolation(Response pdpResponse, String payload) {
    Collection<Result> results = pdpResponse.getResults();
    List<Result> denies = results.stream().filter(result -> result.getDecision().equals(DENY)).collect(toList());
    if (!CollectionUtils.isEmpty(denies)) {
      Collection<Advice> associatedAdvices = denies.get(0).getAssociatedAdvice();
      String associatedAdviceId = CollectionUtils.isEmpty(associatedAdvices) ?
          "No associated advice present on Policy. Please check all policies and repair those without Deny advice" : associatedAdvices.iterator().next().getId().stringValue();
      pdpPolicyViolationRepository.save(new PdpPolicyViolation(associatedAdviceId, payload));
    }
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
