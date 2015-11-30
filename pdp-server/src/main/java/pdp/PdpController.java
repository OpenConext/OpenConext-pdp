package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.openaz.xacml.api.IdReference;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.security.PolicyIdpAccessEnforcer;
import pdp.serviceregistry.ServiceRegistry;
import pdp.shibboleth.ShibbolethUser;
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
  private final PolicyIdpAccessEnforcer policyIdpAccessEnforcer;

  // Can't be final as we need to swap this to reload policies in production
  private PDPEngine pdpEngine;

  @Autowired
  public PdpController(@Value("${initial.delay.policies.refresh.minutes}") int initialDelay,
                       @Value("${period.policies.refresh.minutes}") int period,
                       @Value("${policy.idp.access.enforcement}") boolean policyIdpAccessEnforcement,
                       PdpPolicyViolationRepository pdpPolicyViolationRepository,
                       PdpPolicyRepository pdpPolicyRepository,
                       PDPEngineHolder pdpEngineHolder,
                       ServiceRegistry serviceRegistry) {
    this.pdpEngineHolder = pdpEngineHolder;
    this.pdpEngine = pdpEngineHolder.newPdpEngine();
    this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
    this.policyIdpAccessEnforcer = new PolicyIdpAccessEnforcer(policyIdpAccessEnforcement);
    this.pdpPolicyRepository = pdpPolicyRepository;
    this.serviceRegistry = serviceRegistry;

    newScheduledThreadPool(1).scheduleAtFixedRate(this::refreshPolicies, initialDelay, period, TimeUnit.MINUTES);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/decide/policy")
  public String decide(@RequestBody String payload) throws Exception {
    return doDecide(payload, false);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/internal/decide/policy")
  public String decideInternal(@RequestBody String payload) throws Exception {
    this.refreshPolicies();
    return doDecide(payload, true);
  }

  private String doDecide(String payload, boolean isPlayground) throws Exception {
    long start = System.currentTimeMillis();
    LOG.debug("decide request: {}", payload);

    Request request = JSONRequest.load(payload);

    Response pdpResponse;
    try {
      pdpEngineLock.readLock().lock();
      pdpResponse = pdpEngine.decide(request);
    } finally {
      pdpEngineLock.readLock().unlock();
    }
    String response = JSONResponse.toString(pdpResponse, LOG.isDebugEnabled());
    LOG.debug("decide response: {} took: {} ms", response, System.currentTimeMillis() - start);

    reportPolicyViolation(pdpResponse, response, payload, isPlayground);
    return response;
  }

  @RequestMapping(method = GET, value = "/internal/policies")
  public List<PdpPolicyDefinition> policyDefinitions() {
    Iterable<PdpPolicy> all = pdpPolicyRepository.findAll();
    List<PdpPolicyDefinition> policies = stream(all.spliterator(), false)
        .map(policy -> addEntityMetaData(pdpPolicyDefinitionParser.parse(policy))).collect(toList());

    //can't use Formula - https://issues.jboss.org/browse/JBPAPP-6571
    List<Object[]> countPerPolicyId = pdpPolicyViolationRepository.findCountPerPolicyId();
    Map<Long, Long> countPerPolicyIdMap = countPerPolicyId.stream().collect(toMap((obj) -> (Long) obj[0], (obj) -> (Long) obj[1]));
    policies.forEach(policy -> policy.setNumberOfViolations(countPerPolicyIdMap.getOrDefault(policy.getId(), 0L).intValue()));

    List<Object[]> revisionCountPerId = pdpPolicyRepository.findRevisionCountPerId();
    Map<Number, Number> revisionCountPerIdMap = revisionCountPerId.stream().collect(toMap((obj) -> (Number) obj[0], (obj) -> (Number) obj[1]));
    policies.forEach(policy -> policy.setNumberOfRevisions(revisionCountPerIdMap.getOrDefault(policy.getId().intValue(), 0).intValue()));

    return policies;
  }

  @RequestMapping(method = GET, value = "/internal/violations")
  public Iterable<PdpPolicyViolation> violations() {
    return pdpPolicyViolationRepository.findAll();
  }

  @RequestMapping(method = GET, value = "/internal/violations/{id}")
  public Collection<PdpPolicyViolation> violationsByPolicyId(@PathVariable Long id) {
    PdpPolicy policy = findPolicyById(id);
    return policy.getViolations();
  }

  @RequestMapping(method = GET, value = "/internal/revisions/{id}")
  public Collection<PdpPolicy> revisionsByPolicyId(@PathVariable Long id) {
    PdpPolicy policy = findPolicyById(id);
    return policy.getRevisions();
  }

  @RequestMapping(method = GET, value = "/internal/policies/{id}")
  public PdpPolicyDefinition policyDefinition(@PathVariable Long id) {
    PdpPolicy policy = findPolicyById(id);
    //trigger load - we don't care about the second query being issued
    policy.getRevisions().size();
    PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);
    return addEntityMetaData(policyDefinition);
  }

  @RequestMapping(method = GET, value = "internal/default-policy")
  public PdpPolicyDefinition defaultPolicy() {
    return new PdpPolicyDefinition();
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
      policy = findPolicyById(pdpPolicyDefinition.getId());
      PdpPolicy revision = policy.clone();
      revision.setName(revision.getName() + " r" + policy.getRevisions().size());
      policy.addRevision(revision);
      policy.setName(pdpPolicyDefinition.getName());
      policy.setPolicyId(PolicyTemplateEngine.getPolicyId(pdpPolicyDefinition.getName()));
      policy.setPolicyXml(policyXml);
      policy.setAuthenticatingAuthority(policyIdpAccessEnforcer.authenticatingAuthority());
      policy.setUserIdentifier(policyIdpAccessEnforcer.username());
    } else {
      policy = new PdpPolicy(
          policyXml,
          pdpPolicyDefinition.getName(),
          policyIdpAccessEnforcer.username(),
          policyIdpAccessEnforcer.authenticatingAuthority());
    }
    try {
      //this will throw an Exception if it is not allowed
      this.policyIdpAccessEnforcer.actionAllowed(policy, pdpPolicyDefinition.getServiceProviderId(), pdpPolicyDefinition.getIdentityProviderIds());
      PdpPolicy saved = pdpPolicyRepository.save(policy);
      LOG.info("{} PdpPolicy {}", policy.getId() != null ? "Updated" : "Created", saved.getPolicyXml());
      return saved;
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("pdp_policy_name_unique")) {
        throw new PdpPolicyException("name", "Policy name must be unique. " + pdpPolicyDefinition.getName() + " is already taken");
      } else {
        throw e;
      }
    }
  }

  private PdpPolicy findPolicyById(Long id) {
    PdpPolicy policy = pdpPolicyRepository.findOne(id);
    if (policy == null) {
      throw new PolicyNotFoundException("PdpPolicy with id " + id + " not found");
    }
    return policy;
  }

  @RequestMapping(method = DELETE, value = "/internal/policies/{id}")
  public void deletePdpPolicy(@PathVariable Long id) throws DOMStructureException {
    PdpPolicy policy = findPolicyById(id);

    //we need the sp entityId and (if any) idp entityIds and this is the easiest way to do this
    PdpPolicyDefinition pdpPolicyDefinition = pdpPolicyDefinitionParser.parse(policy);
    //this will throw an Exception if it is not allowed
    this.policyIdpAccessEnforcer.actionAllowed(policy, pdpPolicyDefinition.getServiceProviderId(), pdpPolicyDefinition.getIdentityProviderIds());

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
  public ShibbolethUser user() {
    return (ShibbolethUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private void reportPolicyViolation(Response pdpResponse, String response, String payload, boolean isPlayground) {
    Collection<Result> results = pdpResponse.getResults();
    List<Result> deniesOrIndeterminates = results.stream().filter(result ->
        result.getDecision().equals(DENY) || result.getDecision().equals(INDETERMINATE)).collect(toList());
    if (!CollectionUtils.isEmpty(deniesOrIndeterminates)) {
      Optional<IdReference> idReferenceOptional = getPolicyId(deniesOrIndeterminates);
      if (idReferenceOptional.isPresent()) {
        String policyId = idReferenceOptional.get().getId().stringValue();
        Optional<PdpPolicy> policyOptional = pdpPolicyRepository.findFirstByPolicyId(policyId).stream().findFirst();
        if (policyOptional.isPresent()) {
          pdpPolicyViolationRepository.save(new PdpPolicyViolation(policyOptional.get(), payload, response, isPlayground));
        }
      }
    }
  }

  private Optional<IdReference> getPolicyId(List<Result> deniesOrIndeterminates) {
    Result result = deniesOrIndeterminates.get(0);
    Collection<IdReference> policyIdentifiers = result.getPolicyIdentifiers();
    Collection<IdReference> policySetIdentifiers = result.getPolicySetIdentifiers();
    return !CollectionUtils.isEmpty(policyIdentifiers) ?
        policyIdentifiers.stream().collect(singletonOptionalCollector()) :
        policySetIdentifiers.stream().collect(singletonOptionalCollector());
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
