package pdp;

import org.apache.openaz.xacml.api.IdReference;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.policy.Policy;
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
import pdp.serviceregistry.ServiceRegistry;
import pdp.xacml.PDPEngineHolder;
import pdp.xacml.PdpPolicyDefinitionParser;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.openaz.xacml.api.Decision.DENY;
import static org.apache.openaz.xacml.api.Decision.INDETERMINATE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static pdp.PdpApplication.singletonOptionalCollector;
import static pdp.repositories.PdpPolicyViolationRepository.NO_POLICY_ID;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class PdpController {

  private static Logger LOG = LoggerFactory.getLogger(PdpController.class);
  private final PDPEngineHolder pdpEngineHolder;
  private final PdpPolicyViolationRepository pdpPolicyViolationRepository;
  private final PdpPolicyRepository pdpPolicyRepository;
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final PolicyTemplateEngine policyTemplateEngine = new PolicyTemplateEngine();
  private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();
  private final ServiceRegistry serviceRegistry;

  private PDPEngine pdpEngine;

  @Autowired
  public PdpController(@Value("${initial.delay.policies.refresh.minutes}") int initialDelay,
                       @Value("${period.policies.refresh.minutes}") int period,
                       PdpPolicyViolationRepository pdpPolicyViolationRepository,
                       PdpPolicyRepository pdpPolicyRepository,
                       PDPEngineHolder pdpEngineHolder,
                       ServiceRegistry serviceRegistry) {
    this.pdpEngineHolder = pdpEngineHolder;
    this.pdpEngine = pdpEngineHolder.newPdpEngine();
    this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
    this.pdpPolicyRepository = pdpPolicyRepository;
    this.serviceRegistry = serviceRegistry;

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
  public List<PdpPolicyDefinition> policyDefinitions() {
    Iterable<PdpPolicy> all = pdpPolicyRepository.findAll();
    List<PdpPolicyDefinition> policies = stream(all.spliterator(), false).map(policy -> addEntityMetaData(pdpPolicyDefinitionParser.parse(policy))).collect(toList());
    List<Object[]> countPerPolicyId = pdpPolicyViolationRepository.findCountPerPolicyId();
    //todo - add the count to each policyDefintion
    return policies;
  }

  @RequestMapping(method = GET, value = "/internal/policies/{id}")
  public PdpPolicyDefinition policyDefinition(@PathVariable Long id) {
    PdpPolicy policy = pdpPolicyRepository.findOne(id);
    if (policy == null) {
      throw new PolicyNotFoundException("PdpPolicy with id " + id + " not found");
    }

    return addEntityMetaData(pdpPolicyDefinitionParser.parse(policy));
  }

  @RequestMapping(method = {PUT, POST}, value = "/internal/policies")
  public PdpPolicy createPdpPolicy(@RequestBody PdpPolicyDefinition pdpPolicyDefinition) throws DOMStructureException {
    String policyXml = this.policyTemplateEngine.createPolicyXml(pdpPolicyDefinition);
    //if this works then we know the input was correct
    PdpPolicyDefinitionParser.parsePolicy(policyXml);
    PdpPolicy policy;
    if (pdpPolicyDefinition.getId() != null) {
      policy = pdpPolicyRepository.findOne(pdpPolicyDefinition.getId());
      if (policy == null) {
        throw new PolicyNotFoundException("PdpPolicy with id " + pdpPolicyDefinition.getId() + " not found");
      }
    } else {
      policy = new PdpPolicy(policyXml, pdpPolicyDefinition.getName());
    }
    PdpPolicy saved = pdpPolicyRepository.save(policy);
    LOG.info("Created PdpPolicy {}", saved.getPolicyXml());
    return saved;
  }

  @RequestMapping(method = DELETE, value = "/internal/policies/{id}")
  public void deletePdpPolicy(@PathVariable Long id) throws DOMStructureException {
    PdpPolicy policy = pdpPolicyRepository.findOne(id);
    if (policy == null) {
      throw new PolicyNotFoundException("PdpPolicy with id " + id + " not found");
    }
    LOG.info("Deleting PdpPolicy {}", policy.getName());
    pdpPolicyRepository.delete(policy);
  }

  private PdpPolicyDefinition addEntityMetaData(PdpPolicyDefinition pd) {
    pd.setServiceProviderName(serviceRegistry.serviceProviders().stream().filter(sp ->
        sp.getEntityId().equals(pd.getServiceProviderId())).collect(singletonOptionalCollector()).get().getNameEn());
    pd.setIdentityProviderNames(pd.getIdentityProviderIds().stream().map(idpId ->
        serviceRegistry.identityProviders().stream().filter(idp -> idp.getEntityId().equals(idpId)).collect(singletonOptionalCollector()).get().getNameEn()).collect(toList()));
    return pd;
  }

  @RequestMapping(method = POST, value = "/internal/policies")
  public List<PdpPolicyDefinition> post(@RequestBody @Valid PdpPolicyDefinition policyDefintion) {
    String policyXml = policyTemplateEngine.createPolicyXml(policyDefintion);
    try {
      Policy policyDef = PdpPolicyDefinitionParser.parsePolicy(policyXml);
      if (!policyDef.validate()) {
        throw new RuntimeException("Policy could not be saved because of validation errors in the policyXml " + policyXml);
      }
      pdpPolicyRepository.save(new PdpPolicy(policyXml, policyDefintion.getName()));
      return policyDefinitions();
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
