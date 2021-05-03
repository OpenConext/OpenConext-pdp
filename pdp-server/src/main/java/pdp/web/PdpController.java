package pdp.web;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import org.apache.openaz.xacml.api.Attribute;
import org.apache.openaz.xacml.api.IdReference;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.RequestAttributes;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.policy.Policy;
import org.apache.openaz.xacml.std.IdentifierImpl;
import org.apache.openaz.xacml.std.StdAttribute;
import org.apache.openaz.xacml.std.StdAttributeValue;
import org.apache.openaz.xacml.std.StdMutableAttribute;
import org.apache.openaz.xacml.std.StdMutableRequest;
import org.apache.openaz.xacml.std.StdMutableRequestAttributes;
import org.apache.openaz.xacml.std.StdRequest;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.util.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pdp.JsonMapper;
import pdp.PdpPolicyException;
import pdp.PolicyNotFoundException;
import pdp.access.PolicyAccess;
import pdp.access.PolicyIdpAccessEnforcer;
import pdp.conflicts.PolicyConflictService;
import pdp.domain.EntityMetaData;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.mail.MailBox;
import pdp.manage.Manage;
import pdp.policies.PolicyMissingServiceProviderValidator;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.stats.StatsContext;
import pdp.stats.StatsContextHolder;
import pdp.xacml.PDPEngineHolder;
import pdp.xacml.PdpPolicyDefinitionParser;
import pdp.xacml.PolicyTemplateEngine;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.openaz.xacml.api.Decision.DENY;
import static org.apache.openaz.xacml.api.Decision.INDETERMINATE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static pdp.access.PolicyAccess.READ;
import static pdp.access.PolicyAccess.VIOLATIONS;
import static pdp.access.PolicyAccess.WRITE;
import static pdp.util.StreamUtils.singletonCollector;
import static pdp.util.StreamUtils.singletonOptionalCollector;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class PdpController implements JsonMapper, IPAddressProvider {

    private final static Logger LOG = LoggerFactory.getLogger(PdpController.class);

    private final PDPEngineHolder pdpEngineHolder;
    private final PdpPolicyViolationRepository pdpPolicyViolationRepository;
    private final PdpPolicyRepository pdpPolicyRepository;
    private final PolicyTemplateEngine policyTemplateEngine = new PolicyTemplateEngine();
    private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();
    private final PolicyConflictService policyConflictService = new PolicyConflictService();
    private final Manage manage;
    private final PolicyIdpAccessEnforcer policyIdpAccessEnforcer = new PolicyIdpAccessEnforcer();
    private final PDPEngine playgroundPdpEngine;
    private final boolean cachePolicies;
    private final MailBox mailBox;
    private final PolicyMissingServiceProviderValidator policyMissingServiceProviderValidator;
    private final List<String> loaLevels;

    private final Object reloadingLock = new Object();

    // Can't be final as we need to swap this reference for reloading policies in production
    private volatile PDPEngine pdpEngine;

    @Autowired
    public PdpController(@Value("${period.policies.refresh.minutes}") int period,
                         @Value("${policies.cachePolicies}") boolean cachePolicies,
                         @Value("${loa.levels}") String loaLevelsCommaSeparated,
                         PdpPolicyViolationRepository pdpPolicyViolationRepository,
                         PdpPolicyRepository pdpPolicyRepository,
                         PDPEngineHolder pdpEngineHolder,
                         Manage manage,
                         MailBox mailBox,
                         PolicyMissingServiceProviderValidator policyMissingServiceProviderValidator) {
        this.cachePolicies = cachePolicies;
        this.loaLevels = Stream.of(loaLevelsCommaSeparated.split(",")).map(String::trim).collect(toList());
        this.pdpEngineHolder = pdpEngineHolder;
        this.playgroundPdpEngine = pdpEngineHolder.newPdpEngine(false, true);
        this.pdpEngine = pdpEngineHolder.newPdpEngine(cachePolicies, false);
        this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
        this.pdpPolicyRepository = pdpPolicyRepository;
        this.manage = manage;
        this.mailBox = mailBox;
        this.policyMissingServiceProviderValidator = policyMissingServiceProviderValidator;

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                TaskUtils.decorateTaskWithErrorHandler(this::refreshPolicies, t -> LOG.error("Exception in refreshPolicies task", t), true),
                period, period, TimeUnit.MINUTES);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/decide/policy")
    public String decide(@RequestBody String payload) throws Exception {
        return doDecide(payload, false);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/internal/decide/policy")
    public String decideInternal(@RequestBody String payload) throws Exception {
        refreshPolicies();
        return doDecide(payload, true);
    }

    private String doDecide(String payload, boolean isPlayground) throws Exception {
        StatsContext stats = StatsContextHolder.getContext();

        long start = System.currentTimeMillis();
        LOG.debug("decide request: {}", payload);

        Request request = JSONRequest.load(payload);
        addStatsDetails(stats, request);

        returnPolicyIdInList(request);

        Response pdpResponse = isPlayground ? playgroundPdpEngine.decide(request) : pdpEngine.decide(request);

        String response = JSONResponse.toString(pdpResponse, LOG.isDebugEnabled());

        long took = System.currentTimeMillis() - start;
        stats.setResponseTimeMs(took);
        LOG.debug("decide response: {} took: {} ms", response, took);

        reportPossiblePolicyViolation(pdpResponse, response, payload, isPlayground);
        provideStatsContext(stats, pdpResponse);

        return response;
    }

    private void returnPolicyIdInList(Request request) {
        StdRequest.class.cast(request);
        Field field = ReflectionUtils.findField(Wrapper.class, "wrappedObject");
        ReflectionUtils.makeAccessible(field);
        StdMutableRequest mutableRequest = StdMutableRequest.class.cast(ReflectionUtils.getField(field, request));
        mutableRequest.setReturnPolicyIdList(true);
        StdMutableRequestAttributes requestAttributes = (StdMutableRequestAttributes) mutableRequest.getRequestAttributes().stream().filter(requestAttribute ->
                requestAttribute.getCategory().getUri().toString().equals("urn:oasis:names:tc:xacml:3.0:attribute-category:resource"))
                .findFirst().get();
        Collection<Attribute> attributes = requestAttributes.getAttributes();
        boolean clientIDPresent = attributes.stream().anyMatch(attribute -> attribute.getAttributeId().getUri().toString().equals("ClientID"));
        if (!clientIDPresent) {
            requestAttributes.add(new StdAttribute(
                    new StdMutableAttribute(
                            new IdentifierImpl("urn:oasis:names:tc:xacml:3.0:attribute-category:resource"),
                            new IdentifierImpl("ClientID"),
                            new StdAttributeValue<>(
                                    new IdentifierImpl("http://www.w3.org/2001/XMLSchema#string"),
                                    "EngineBlock")
                    )));
        }
    }

    private void provideStatsContext(StatsContext stats, Response pdpResponse) {
        Result result = pdpResponse.getResults().iterator().next();
        stats.setDecision(result.getDecision().toString());

        Optional<String> optionalLoa = getOptionalLoa(pdpResponse);
        optionalLoa.ifPresent(stats::setLoa);

        Optional<IdReference> optionalPolicyId = getPolicyId(result);
        optionalPolicyId.ifPresent(policyId -> stats.setPolicyId(policyId.getId().getUri().toString()));
    }

    @RequestMapping(method = OPTIONS, value = "/protected/policies")
    public ResponseEntity<Void> options(HttpServletResponse response) {
        response.setHeader(HttpHeaders.ALLOW, Joiner.on(",").join(ImmutableList.of(GET, POST, PUT, DELETE)));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = GET, value = {"/internal/policies", "/protected/policies"})
    public List<PdpPolicyDefinition> policyDefinitions() {
        List<PdpPolicyDefinition> policies = pdpPolicyRepository.findAll().stream()
                .map(policy -> {
                    PdpPolicyDefinition pdpPolicyDefinition = pdpPolicyDefinitionParser.parse(policy);
                    return addAccessRules(policy, pdpPolicyDefinition);
                }).collect(toList());

        policies = policyMissingServiceProviderValidator.addEntityMetaData(policies);

        policies = policies.stream().filter(policy -> !policy.isServiceProviderInvalidOrMissing()).collect(toList());

        //can't use Formula - https://issues.jboss.org/browse/JBPAPP-6571
        List<Object[]> countPerPolicyId = pdpPolicyViolationRepository.findCountPerPolicyId();
        Map<Long, Long> countPerPolicyIdMap = countPerPolicyId.stream().collect(toMap((obj) -> (Long) obj[0], (obj) -> (Long) obj[1]));
        policies.forEach(policy -> policy.setNumberOfViolations(countPerPolicyIdMap.getOrDefault(policy.getId(), 0L).intValue()));

        List<Object[]> revisionCountPerId = pdpPolicyRepository.findRevisionCountPerId();
        Map<Number, Number> revisionCountPerIdMap = revisionCountPerId.stream().collect(toMap((obj) -> (Number) obj[0], (obj) -> (Number) obj[1]));
        policies.forEach(policy -> policy.setNumberOfRevisions(revisionCountPerIdMap.getOrDefault(policy.getId().intValue(), 0).intValue()));

        return policyIdpAccessEnforcer.filterPdpPolicies(policies);
    }

    @RequestMapping(method = GET, value = {"/internal/conflicts", "/protected/conflicts"})
    public Map<String, List<PdpPolicyDefinition>> conflicts() {
        return doConflicts(true);
    }

    private Map<String, List<PdpPolicyDefinition>> doConflicts(boolean includeInvalid) {
        List<PdpPolicyDefinition> policies = pdpPolicyRepository.findAll().stream()
                .map(policy -> pdpPolicyDefinitionParser.parse(policy)).collect(toList());

        policies = policyMissingServiceProviderValidator.addEntityMetaData(policies);

        Map<String, List<PdpPolicyDefinition>> conflicts = policyConflictService.conflicts(policies);
        if (includeInvalid) {
            List<PdpPolicyDefinition> invalid = policies.stream()
                    .filter(PdpPolicyDefinition::isServiceProviderInvalidOrMissing).collect(toList());
            if (!invalid.isEmpty()) {
                conflicts.put("EntityID not present in Manage", invalid);
            }
        }
        return conflicts;
    }

    @RequestMapping(method = {PUT, POST}, value = {"/internal/policies", "/protected/policies"})
    public PdpPolicy createPdpPolicy(@RequestBody PdpPolicyDefinition pdpPolicyDefinition) {
        String policyXml = policyTemplateEngine.createPolicyXml(pdpPolicyDefinition);
        //if this works then we know the input was correct
        Policy parsedPolicy = pdpPolicyDefinitionParser.parsePolicy(policyXml);
        Assert.notNull(parsedPolicy, "ParsedPolicy is not valid");

        PdpPolicy policy;
        if (pdpPolicyDefinition.getId() != null) {
            PdpPolicy fromDB = findPolicyById(pdpPolicyDefinition.getId(), WRITE);
            policy = fromDB.getParentPolicy() != null ? fromDB.getParentPolicy() : fromDB;
            //Cascade.ALL
            PdpPolicy.revision(pdpPolicyDefinition.getName(), policy, policyXml, policyIdpAccessEnforcer.username(),
                    policyIdpAccessEnforcer.userDisplayName(), pdpPolicyDefinition.isActive());
        } else {
            policy = new PdpPolicy(policyXml, pdpPolicyDefinition.getName(), true, policyIdpAccessEnforcer.username(),
                    policyIdpAccessEnforcer.authenticatingAuthority(), policyIdpAccessEnforcer.userDisplayName(), pdpPolicyDefinition.isActive(),
                    pdpPolicyDefinition.getType());

            //this will throw an Exception if it is not allowed
            policyIdpAccessEnforcer.actionAllowed(policy, PolicyAccess.WRITE, pdpPolicyDefinition.getServiceProviderId(), pdpPolicyDefinition.getIdentityProviderIds());
        }

        try {
            PdpPolicy saved = pdpPolicyRepository.save(policy);
            LOG.info("{} PdpPolicy {}", policy.getId() != null ? "Updated" : "Created", saved.getPolicyXml());
            checkConflicts(pdpPolicyDefinition);
            return saved;
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("pdp_policy_name_revision_unique")) {
                throw new PdpPolicyException("name", "Policy name must be unique. " + pdpPolicyDefinition.getName() + " is already taken");
            } else {
                throw e;
            }
        }
    }

    private void checkConflicts(PdpPolicyDefinition pdpPolicyDefinition) {
        Map<String, List<PdpPolicyDefinition>> conflicts = this.doConflicts(false);
        Optional<EntityMetaData> entityMetaData = manage.serviceProviderOptionalByEntityId(pdpPolicyDefinition.getServiceProviderId());
        if (entityMetaData.isPresent() && conflicts.containsKey(entityMetaData.get().getNameEn())) {
            this.mailBox.sendConflictsMail(conflicts);
        }
    }

    @RequestMapping(method = GET, value = {"/internal/policies/{id}", "/protected/policies/{id}"})
    public PdpPolicyDefinition policyDefinition(@PathVariable Long id) {
        PdpPolicy policyById = findPolicyById(id, READ);
        PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policyById);
        policyDefinition = policyMissingServiceProviderValidator.addEntityMetaData(Collections.singletonList(policyDefinition)).get(0);
        if (policyDefinition.getType().equals("step")) {
            policyDefinition.getLoas().forEach(loa -> loa.getCidrNotations()
                    .forEach(notation -> notation.setIpInfo(getIpInfo(notation.getIpAddress(), notation.getPrefix()))));
        }
        if (!policyById.isLatestRevision()) {
            PdpPolicy latestPolicy =
                    policyById.getRevisions().stream().max(Comparator.comparing(PdpPolicy::getRevisionNbr)).get();
            policyDefinition.setParentId(latestPolicy.getId());
        }
        return policyDefinition;
    }

    @RequestMapping(method = DELETE, value = {"/internal/policies/{id}", "/protected/policies/{id}"})
    public void deletePdpPolicy(@PathVariable Long id) {
        PdpPolicy policy = findPolicyById(id, PolicyAccess.WRITE);

        LOG.info("Deleting PdpPolicy {}", policy.getName());
        policy = policy.getParentPolicy() != null ? policy.getParentPolicy() : policy;
        pdpPolicyRepository.delete(policy);
    }

    @RequestMapping(method = GET, value = "/internal/default-policy/{type}")
    public PdpPolicyDefinition defaultPolicy(@PathVariable String type) {
        PdpPolicyDefinition pdpPolicyDefinition = new PdpPolicyDefinition();
        pdpPolicyDefinition.setType(type);
        return pdpPolicyDefinition;
    }

    @RequestMapping(method = GET, value = "/internal/policies/sp")
    public List<PdpPolicyDefinition> policyDefinitionsByServiceProvider(@RequestParam String serviceProvider) {
        List<PdpPolicyDefinition> policies = policyDefinitions();

        List<PdpPolicyDefinition> filterBySp = policies.stream().filter(policy -> policy.getServiceProviderId().equals(serviceProvider)).collect(toList());

        return policyIdpAccessEnforcer.filterPdpPolicies(filterBySp);
    }

    @RequestMapping(method = GET, value = "/internal/violations")
    public Iterable<PdpPolicyViolation> violations() {
        Iterable<PdpPolicyViolation> violations = pdpPolicyViolationRepository.findAll();
        return policyIdpAccessEnforcer.filterViolations(violations);
    }

    @RequestMapping(method = GET, value = "/internal/violations/{id}")
    public Iterable<PdpPolicyViolation> violationsByPolicyId(@PathVariable Long id) {
        Set<PdpPolicyViolation> violations = findPolicyById(id, VIOLATIONS).getViolations();
        return policyIdpAccessEnforcer.filterViolations(violations);
    }

    @RequestMapping(method = GET, value = {"/internal/revisions/{id}", "/protected/revisions/{id}"})
    public List<PdpPolicyDefinition> revisionsByPolicyId(@PathVariable Long id) {
        PdpPolicy policy = findPolicyById(id, PolicyAccess.READ);
        PdpPolicy parent = (policy.getParentPolicy() != null ? policy.getParentPolicy() : policy);

        Set<PdpPolicy> policies = parent.getRevisions();
        policies.add(parent);
        List<PdpPolicyDefinition> definitions = policies.stream().map(rev -> {
            PdpPolicyDefinition def = pdpPolicyDefinitionParser.parse(rev);
            return addAccessRules(rev, def);
        }).collect(toList());
        return policyMissingServiceProviderValidator.addEntityMetaData(definitions);
    }

    @RequestMapping(method = GET, value = {"/internal/loas", "/protected/loas"})
    public List<String> allowedLevelOfAssurances() {
        return this.loaLevels;
    }

    @RequestMapping(method = GET, value = {"/internal/attributes", "/protected/attributes"})
    public List<JsonPolicyRequest.Attribute> allowedAttributes() throws IOException {
        InputStream inputStream = new ClassPathResource("xacml/attributes/allowed_attributes.json").getInputStream();
        CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, JsonPolicyRequest.Attribute.class);
        return objectMapper.readValue(inputStream, type);
    }

    @RequestMapping(method = GET, value = "/internal/saml-attributes")
    public List<JsonPolicyRequest.Attribute> allowedSamlAttributes() throws IOException {
        InputStream inputStream = new ClassPathResource("xacml/attributes/extra_saml_attributes.json").getInputStream();
        CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, JsonPolicyRequest.Attribute.class);
        List<JsonPolicyRequest.Attribute> attributes = objectMapper.readValue(inputStream, type);
        attributes.addAll(allowedAttributes());
        return attributes;
    }

    private PdpPolicy findPolicyById(Long id, PolicyAccess policyAccess) {
        PdpPolicy policy = pdpPolicyRepository.findOne(id);
        if (policy == null) {
            throw new PolicyNotFoundException("PdpPolicy with id " + id + " not found");
        }
        PdpPolicyDefinition definition = pdpPolicyDefinitionParser.parse(policy);
        //this will throw an Exception if it is not allowed
        policyIdpAccessEnforcer.actionAllowed(policy, policyAccess, definition.getServiceProviderId(), definition.getIdentityProviderIds());
        return policy;
    }

    private PdpPolicyDefinition addAccessRules(PdpPolicy policy, PdpPolicyDefinition pd) {
        boolean actionsAllowed = policyIdpAccessEnforcer.actionAllowedIndicator(policy, PolicyAccess.WRITE, pd.getServiceProviderId(), pd.getIdentityProviderIds());
        pd.setActionsAllowed(actionsAllowed);
        return pd;
    }

    private void addStatsDetails(StatsContext stats, Request request) {
        RequestAttributes req = request.getRequestAttributes().stream()
                .filter(ra -> ra.getCategory().getUri().toString().equals("urn:oasis:names:tc:xacml:3.0:attribute-category:resource"))
                .collect(singletonCollector());
        Collection<Attribute> attributes = req.getAttributes();
        stats.setIdentityProvider(getAttributeValue(attributes, "IDPentityID").orElse(""));
        stats.setServiceProvicer(getAttributeValue(attributes, "SPentityID").orElse(""));
    }

    private Optional<String> getAttributeValue(Collection<Attribute> attributes, String attributeId) {
        Optional<Attribute> attribute = attributes.stream().filter(attr -> attr.getAttributeId().getUri().toString().equals(attributeId)).collect(singletonOptionalCollector());
        return attribute.map(attr -> (String) attr.getValues().iterator().next().getValue());
    }

    private void reportPossiblePolicyViolation(Response pdpResponse, String response, String payload, boolean isPlayground) {
        Collection<Result> results = pdpResponse.getResults();

        Optional<Result> deniesOrIndeterminate = results.stream().filter(result ->
                result.getDecision().equals(DENY) || result.getDecision().equals(INDETERMINATE)).findAny();
        deniesOrIndeterminate.ifPresent(result -> {
            Optional<IdReference> idReferenceOptional = getPolicyId(result);
            idReferenceOptional.ifPresent(idReference -> {
                String policyId = idReference.getId().stringValue();
                Optional<PdpPolicy> policyOptional = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true);
                policyOptional.ifPresent(policy -> pdpPolicyViolationRepository.save(new PdpPolicyViolation(policy, payload, response, isPlayground)));
            });
        });
    }

    private Optional<String> getOptionalLoa(Response pdpResponse) {
        return pdpResponse.getResults().stream().map(result -> result.getObligations().stream()
                .map(obligation -> obligation.getAttributeAssignments().stream()
                        .map(attributeAssignment -> String.class.cast(attributeAssignment.getAttributeValue().getValue()))))
                .flatMap(Function.identity())
                .flatMap(Function.identity())
                .max(Comparator.naturalOrder());
    }

    private Optional<IdReference> getPolicyId(Result deniesOrIndeterminate) {
        Collection<IdReference> policyIdentifiers = deniesOrIndeterminate.getPolicyIdentifiers();
        Collection<IdReference> policySetIdentifiers = deniesOrIndeterminate.getPolicySetIdentifiers();
        return !CollectionUtils.isEmpty(policyIdentifiers) ?
                policyIdentifiers.stream().collect(singletonOptionalCollector()) :
                policySetIdentifiers.stream().collect(singletonOptionalCollector());
    }

    private void refreshPolicies() {
        synchronized (reloadingLock) {
            LOG.info("Starting reloading policies");
            long start = System.currentTimeMillis();
            this.pdpEngine = pdpEngineHolder.newPdpEngine(cachePolicies, false);
            LOG.info("Finished reloading policies in {} ms", System.currentTimeMillis() - start);
        }
    }

}
