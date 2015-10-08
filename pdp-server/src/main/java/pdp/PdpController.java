package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.openaz.xacml.api.IdReference;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.std.StdMutableRequest;
import org.apache.openaz.xacml.std.StdRequest;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.serviceregistry.ServiceRegistry;
import pdp.xacml.PDPEngineHolder;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.apache.openaz.xacml.api.Decision.DENY;
import static org.apache.openaz.xacml.api.Decision.INDETERMINATE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static pdp.PdpApplication.singletonOptionalCollector;
import static pdp.repositories.PdpPolicyViolationRepository.NO_POLICY_ID;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class PdpController {

  private final static Logger LOG = LoggerFactory.getLogger(PdpController.class);
  private final static ObjectMapper objectMapper = new ObjectMapper();

  private final PDPEngineHolder pdpEngineHolder;
  private final PdpPolicyViolationRepository pdpPolicyViolationRepository;
  private final PdpPolicyRepository pdpPolicyRepository;
  private final ReadWriteLock pdpEngineLock = new ReentrantReadWriteLock();
  private final PolicyTemplateEngine policyTemplateEngine = new PolicyTemplateEngine();
  private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();
  private final ServiceRegistry serviceRegistry;

  // Can't be final as we need to swap this to reload policies in production
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

    newScheduledThreadPool(1).scheduleAtFixedRate(this::refreshPolicies, initialDelay, period, TimeUnit.MINUTES);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/decide/policy")
  public String decide(@RequestBody String payload) throws Exception {
    return doDecide(payload);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/internal/decide/policy")
  public String decideInternal(@RequestBody String payload) throws Exception {
    this.refreshPolicies();
    return doDecide(payload);
  }

  private String doDecide(String payload) throws Exception {
    long start = System.currentTimeMillis();
    LOG.debug("decide request: {}", payload);

    Request request = JSONRequest.load(payload);

    if (!request.getReturnPolicyIdList()) {
      request = createReturnPolicyIdListRequest(request);
    }

    Response pdpResponse;
    try {
      pdpEngineLock.readLock().lock();
      pdpResponse = pdpEngine.decide(request);
    } finally {
      pdpEngineLock.readLock().unlock();
    }
    String response = JSONResponse.toString(pdpResponse, LOG.isDebugEnabled());
    LOG.debug("decide response: {} took: {} ms", response, System.currentTimeMillis() - start);

    reportPolicyViolation(pdpResponse, response, payload);
    return response;
  }

  private Request createReturnPolicyIdListRequest(Request originalRequest) {
    StdMutableRequest request = new StdMutableRequest();
    originalRequest.getRequestAttributes().stream().forEach(request::add);
    request.setReturnPolicyIdList(true);
    return new StdRequest(originalRequest);
  }

  @RequestMapping(method = GET, value = "/internal/policies")
  public List<PdpPolicyDefinition> policyDefinitions() {
    Iterable<PdpPolicy> all = pdpPolicyRepository.findAll();
    List<PdpPolicyDefinition> policies = stream(all.spliterator(), false).map(policy -> addEntityMetaData(pdpPolicyDefinitionParser.parse(policy))).collect(toList());

    Map<String, Long> countPerPolicyIdMap = pdpPolicyViolationRepository.findCountPerPolicyId().stream().collect(toMap((objects) -> (String) objects[0], (objects) -> (Long) objects[1]));

    policies.forEach(policy -> policy.setNumberOfViolations(countPerPolicyIdMap.getOrDefault(policy.getNameId(), 0L).intValue()));
    return policies;
  }

  @RequestMapping(method = GET, value = "/internal/policies/{id}")
  public PdpPolicyDefinition policyDefinition(@PathVariable Long id) {
    PdpPolicy policy = findOneAndOnly(id);
    return addEntityMetaData(pdpPolicyDefinitionParser.parse(policy));
  }

  @RequestMapping(method = GET, value = "internal/default-policy")
  public PdpPolicyDefinition defaultPolicy() {
    PdpPolicyDefinition definition = new PdpPolicyDefinition();
    return definition;
  }

  @RequestMapping(method = GET, value = "internal/attributes")
  public List<JsonPolicyRequest.Attribute> allowedAttributes() throws IOException {
    InputStream inputStream = new ClassPathResource("xacml/attributes/allowed_attributes.json").getInputStream();
    CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, JsonPolicyRequest.Attribute.class);
    return objectMapper.readValue(inputStream, type);
  }

  @RequestMapping(method = GET, value = "internal/saml-attributes")
  public List<JsonPolicyRequest.Attribute> allowedSamlAttributes() throws IOException {
    InputStream inputStream = new ClassPathResource("xacml/attributes/extra_saml_attributes.json").getInputStream();
    CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, JsonPolicyRequest.Attribute.class);
    List<JsonPolicyRequest.Attribute> attributes = objectMapper.readValue(inputStream, type);
    attributes.addAll(allowedAttributes());
    return attributes;
  }

  @RequestMapping(method = {PUT, POST}, value = "/internal/policies")
  public PdpPolicy createPdpPolicy(@RequestBody PdpPolicyDefinition pdpPolicyDefinition) throws DOMStructureException {
    String policyXml = this.policyTemplateEngine.createPolicyXml(pdpPolicyDefinition);
    //if this works then we know the input was correct
    PdpPolicyDefinitionParser.parsePolicy(policyXml);
    PdpPolicy policy;
    if (pdpPolicyDefinition.getId() != null) {
      policy = findOneAndOnly(pdpPolicyDefinition.getId());
      policy.setName(pdpPolicyDefinition.getName());
      policy.setPolicyXml(policyXml);
    } else {
      policy = new PdpPolicy(policyXml, pdpPolicyDefinition.getName());
    }
    try {
      PdpPolicy saved = pdpPolicyRepository.save(policy);
      LOG.info("{} PdpPolicy {}",policy.getId() != null ? "Updated" : "Created", saved.getPolicyXml());
      return saved;
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("pdp_policy_name_unique")) {
        throw new PdpPolicyException("name", "Policy name must be unique. " + pdpPolicyDefinition.getName() + " is already taken");
      } else {
        throw e;
      }
    }
  }

  private PdpPolicy findOneAndOnly(Long id) {
    PdpPolicy policy = pdpPolicyRepository.findOne(id);
    if (policy == null) {
      throw new PolicyNotFoundException("PdpPolicy with id " + id + " not found");
    }
    return policy;
  }

  @RequestMapping(method = DELETE, value = "/internal/policies/{id}")
  public void deletePdpPolicy(@PathVariable Long id) throws DOMStructureException {
    PdpPolicy policy = findOneAndOnly(id);
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
    pdpEngineLock.writeLock().lock();
    try {
      this.pdpEngine = pdpEngineHolder.newPdpEngine();
    } finally {
      pdpEngineLock.writeLock().unlock();
    }
    LOG.info("Finished reloading policies in {} ms", System.currentTimeMillis() - start);
  }

}
