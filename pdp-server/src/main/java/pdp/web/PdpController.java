package pdp.web;

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
import pdp.PdpPolicyException;
import pdp.PolicyNotFoundException;
import pdp.access.FederatedUser;
import pdp.access.PolicyIdpAccessEnforcer;
import pdp.domain.*;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.serviceregistry.ServiceRegistry;
import pdp.xacml.PDPEngineHolder;
import pdp.xacml.PdpPolicyDefinitionParser;
import pdp.xacml.PolicyTemplateEngine;

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
import static pdp.util.StreamUtils.singletonOptionalCollector;

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
  private final boolean policyIncludeAggregatedAttributes;

  // Can't be final as we need to swap this to reload policies in production
  private PDPEngine pdpEngine;

  @Autowired
  public PdpController(@Value("${period.policies.refresh.minutes}") int period,
                       @Value("${policy.include.aggregated.attributes}") boolean policyIncludeAggregatedAttributes,
                       PdpPolicyViolationRepository pdpPolicyViolationRepository,
                       PdpPolicyRepository pdpPolicyRepository,
                       PDPEngineHolder pdpEngineHolder,
                       ServiceRegistry serviceRegistry) {
    this.pdpEngineHolder = pdpEngineHolder;
    this.pdpEngine = pdpEngineHolder.newPdpEngine(policyIncludeAggregatedAttributes);
    this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
    this.policyIdpAccessEnforcer = new PolicyIdpAccessEnforcer(serviceRegistry);
    this.policyIncludeAggregatedAttributes = policyIncludeAggregatedAttributes;
    this.pdpPolicyRepository = pdpPolicyRepository;
    this.serviceRegistry = serviceRegistry;

    newScheduledThreadPool(1).scheduleAtFixedRate(this::refreshPolicies, period, period, TimeUnit.MINUTES);
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
    List<PdpPolicyDefinition> policies = stream(pdpPolicyRepository.findAll().spliterator(), false)
        .map(policy -> addEntityMetaData(addAccessRules(policy, pdpPolicyDefinitionParser.parse(policy)))).collect(toList());

    //can't use Formula - https://issues.jboss.org/browse/JBPAPP-6571
    List<Object[]> countPerPolicyId = pdpPolicyViolationRepository.findCountPerPolicyId();
    Map<Long, Long> countPerPolicyIdMap = countPerPolicyId.stream().collect(toMap((obj) -> (Long) obj[0], (obj) -> (Long) obj[1]));
    policies.forEach(policy -> policy.setNumberOfViolations(countPerPolicyIdMap.getOrDefault(policy.getId(), 0L).intValue()));

    List<Object[]> revisionCountPerId = pdpPolicyRepository.findRevisionCountPerId();
    Map<Number, Number> revisionCountPerIdMap = revisionCountPerId.stream().collect(toMap((obj) -> (Number) obj[0], (obj) -> (Number) obj[1]));
    policies.forEach(policy -> policy.setNumberOfRevisions(revisionCountPerIdMap.getOrDefault(policy.getId().intValue(), 0).intValue()));

    return this.policyIdpAccessEnforcer.filterPdpPolicies(policies);
  }

  @RequestMapping(method = GET, value = "/internal/policies/sp")
  public List<PdpPolicyDefinition> policyDefinitionsByServiceProvider(@RequestParam String serviceProvider) {
    List<PdpPolicyDefinition> policies = policyDefinitions();

    List<PdpPolicyDefinition> filterBySp = stream(policies.spliterator(), false).filter(policy -> policy.getServiceProviderId().equals(serviceProvider)).collect(toList());

    return this.policyIdpAccessEnforcer.filterPdpPolicies(filterBySp);
  }

  @RequestMapping(method = GET, value = "/internal/violations")
  public Iterable<PdpPolicyViolation> violations() {
    Iterable<PdpPolicyViolation> violations = pdpPolicyViolationRepository.findAll();
    return this.policyIdpAccessEnforcer.filterViolations(violations);
  }

  @RequestMapping(method = GET, value = "/internal/violations/{id}")
  public Iterable<PdpPolicyViolation> violationsByPolicyId(@PathVariable Long id) {
    Set<PdpPolicyViolation> violations = findPolicyById(id).getViolations();
    return this.policyIdpAccessEnforcer.filterViolations(violations);
  }

  @RequestMapping(method = GET, value = "/internal/revisions/{id}")
  public List<PdpPolicyDefinition> revisionsByPolicyId(@PathVariable Long id) {
    PdpPolicy policy = findPolicyById(id);
    PdpPolicy parent = policy.getParentPolicy();
    Set<PdpPolicy> revisions = parent != null ? parent.getRevisions() : policy.getRevisions();
    List<PdpPolicyDefinition> definitions = revisions.stream().map(rev -> addEntityMetaData(pdpPolicyDefinitionParser.parse(rev))).collect(toList());

    definitions.add(addEntityMetaData(pdpPolicyDefinitionParser.parse(parent != null ? parent : policy)));
    return definitions;
  }

  @RequestMapping(method = GET, value = "/internal/policies/{id}")
  public PdpPolicyDefinition policyDefinition(@PathVariable Long id) {
    return addEntityMetaData(pdpPolicyDefinitionParser.parse(findPolicyById(id)));
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
      PdpPolicy fromDB = findPolicyById(pdpPolicyDefinition.getId());
      policy = fromDB.getParentPolicy() != null ? fromDB.getParentPolicy() : fromDB;
      //Cascade.ALL
      PdpPolicy.revision(pdpPolicyDefinition.getName(), policy, policyXml, policyIdpAccessEnforcer.username(),
          policyIdpAccessEnforcer.authenticatingAuthority(), policyIdpAccessEnforcer.userDisplayName(), pdpPolicyDefinition.isActive());
    } else {
      policy = new PdpPolicy(policyXml, pdpPolicyDefinition.getName(), true, policyIdpAccessEnforcer.username(),
          policyIdpAccessEnforcer.authenticatingAuthority(), policyIdpAccessEnforcer.userDisplayName(), pdpPolicyDefinition.isActive());
    }
    try {
      //this will throw an Exception if it is not allowed
      this.policyIdpAccessEnforcer.actionAllowed(policy, pdpPolicyDefinition.getServiceProviderId(), pdpPolicyDefinition.getIdentityProviderIds());
      PdpPolicy saved = pdpPolicyRepository.save(policy);
      LOG.info("{} PdpPolicy {}", policy.getId() != null ? "Updated" : "Created", saved.getPolicyXml());
      return saved;
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("pdp_policy_name_revision_unique")) {
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
    PdpPolicyDefinition definition = pdpPolicyDefinitionParser.parse(policy);
    //this will throw an Exception if it is not allowed
    this.policyIdpAccessEnforcer.actionAllowed(policy, definition.getServiceProviderId(), definition.getIdentityProviderIds());
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
    policy = policy.getParentPolicy() != null ? policy.getParentPolicy() : policy;
    pdpPolicyRepository.delete(policy);
  }

  private PdpPolicyDefinition addAccessRules(PdpPolicy policy, PdpPolicyDefinition pd) {
    boolean actionsAllowed = this.policyIdpAccessEnforcer.actionAllowedIndicator(policy, pd.getServiceProviderId(), pd.getIdentityProviderIds());
    pd.setActionsAllowed(actionsAllowed);
    return pd;
  }

  private PdpPolicyDefinition addEntityMetaData(PdpPolicyDefinition pd) {
    EntityMetaData sp = serviceRegistry.serviceProviders().stream().filter(md ->
        md.getEntityId().equals(pd.getServiceProviderId())).collect(singletonOptionalCollector()).get();
    pd.setServiceProviderName(sp.getNameEn());
    pd.setActivatedSr(sp.isPolicyEnforcementDecisionRequired());
    pd.setIdentityProviderNames(pd.getIdentityProviderIds().stream().map(idpId ->
        serviceRegistry.identityProviders().stream().filter(idp -> idp.getEntityId().equals(idpId)).collect(singletonOptionalCollector()).get().getNameEn()).collect(toList()));
    return pd;
  }

  @RequestMapping(method = GET, value = "internal/users/me")
  public FederatedUser user() {
    return (FederatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private void reportPolicyViolation(Response pdpResponse, String response, String payload, boolean isPlayground) {
    Collection<Result> results = pdpResponse.getResults();
    List<Result> deniesOrIndeterminates = results.stream().filter(result ->
        result.getDecision().equals(DENY) || result.getDecision().equals(INDETERMINATE)).collect(toList());
    if (!CollectionUtils.isEmpty(deniesOrIndeterminates)) {
      Optional<IdReference> idReferenceOptional = getPolicyId(deniesOrIndeterminates);
      if (idReferenceOptional.isPresent()) {
        String policyId = idReferenceOptional.get().getId().stringValue();
        Optional<PdpPolicy> policyOptional = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).stream().findFirst();
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
      this.pdpEngine = pdpEngineHolder.newPdpEngine(policyIncludeAggregatedAttributes);
    } finally {
      pdpEngineLock.writeLock().unlock();
    }
    LOG.info("Finished reloading policies in {} ms", System.currentTimeMillis() - start);
  }

}
