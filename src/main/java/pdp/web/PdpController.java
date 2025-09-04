package pdp.web;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.openaz.xacml.api.*;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.policy.Policy;
import org.apache.openaz.xacml.std.*;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.util.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pdp.JsonMapper;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.repositories.PdpPolicyPushVersionRepository;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.stats.StatsContext;
import pdp.stats.StatsContextHolder;
import pdp.xacml.PDPEngineHolder;
import pdp.xacml.PdpPolicyDefinitionParser;
import pdp.xacml.PolicyTemplateEngine;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.apache.openaz.xacml.api.Decision.DENY;
import static org.apache.openaz.xacml.api.Decision.INDETERMINATE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
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
    private final PDPEngine playgroundPdpEngine;
    private final boolean cachePolicies;

    private final ReentrantLock lock = new ReentrantLock();
    private final PdpPolicyPushVersionRepository pdpPolicyPushVersionRepository;

    // Can't be final as we need to swap this reference for reloading policies in production
    private volatile PDPEngine pdpEngine;
    private final AtomicLong policiesPushVersion = new AtomicLong();

    @Autowired
    public PdpController(@Value("${period.policies.refresh.minutes}") int period,
                         @Value("${policies.cachePolicies}") boolean cachePolicies,
                         PdpPolicyViolationRepository pdpPolicyViolationRepository,
                         PdpPolicyRepository pdpPolicyRepository,
                         PDPEngineHolder pdpEngineHolder,
                         PdpPolicyPushVersionRepository pdpPolicyPushVersionRepository) {
        this.cachePolicies = cachePolicies;
        this.pdpEngineHolder = pdpEngineHolder;
        this.playgroundPdpEngine = pdpEngineHolder.newPdpEngine(false, true);
        this.pdpEngine = pdpEngineHolder.newPdpEngine(cachePolicies, false);
        this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
        this.pdpPolicyRepository = pdpPolicyRepository;
        this.pdpPolicyPushVersionRepository = pdpPolicyPushVersionRepository;
        this.policiesPushVersion.set(pdpPolicyPushVersionRepository.findById(1L).get().getVersion());

    }

    @RequestMapping(method = RequestMethod.POST, value = "/decide/policy")
    public String decide(@RequestBody String payload) throws Exception {
        Long currentPoliciesPushVersion = pdpPolicyPushVersionRepository.findById(1L).get().getVersion();
        if (currentPoliciesPushVersion != this.policiesPushVersion.get()) {
            refreshPolicies();
        }
        return doDecide(payload, false);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/manage/decide")
    public String decideManage(@RequestBody String payload) throws Exception {
        return doDecide(payload, true);
    }

    private String doDecide(String payload, boolean isPlayground) throws Exception {
        StatsContext stats = StatsContextHolder.getContext();

        long start = System.currentTimeMillis();
        LOG.debug("decide request: {}", payload);

        Request request = JSONRequest.load(payload);
        addStatsDetails(stats, request);

        returnPolicyIdInList(request);
        Response pdpResponse;
        try {
            lock.lock();
            pdpResponse = isPlayground ? playgroundPdpEngine.decide(request) : pdpEngine.decide(request);
        } finally {
            lock.unlock();
        }

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

    @RequestMapping(method = PUT, value = {"/manage/push"})
    @Transactional
    public void pushPolicyDefinitions(@RequestBody List<PdpPolicyDefinition> policyDefinitions) {
        LOG.info("/manage/push");
        List<PdpPolicy> policies = policyDefinitions.stream()
                .map(policyDefinition -> {
                    if (CollectionUtils.isEmpty(policyDefinition.getServiceProviderIds())) {
                        throw new IllegalArgumentException(
                                String.format("Policy %s has no serviceProviderIds. This is invalid",
                                        policyDefinition.getName())
                        );
                    }
                    String policyXml = policyTemplateEngine.createPolicyXml(policyDefinition);
                    Policy parsedPolicy = pdpPolicyDefinitionParser.parsePolicy(policyXml, policyDefinition.getName());
                    //If there are null's then something is wrong
                    Assert.notNull(parsedPolicy, "ParsedPolicy is not valid");
                    return new PdpPolicy(
                            policyXml,
                            policyDefinition.getName(),
                            true,
                            policyDefinition.getUserDisplayName(),
                            policyDefinition.getAuthenticatingAuthorityName(),
                            "manage",
                            policyDefinition.isActive(),
                            policyDefinition.getType());
                }).collect(toList());
        //Refresh, but also increment the counter so other instances will refresh also
        pdpPolicyRepository.deleteAll();
        pdpPolicyRepository.saveAll(policies);

        LOG.info("/manage/push saved policies:" + policies.size());

        this.pdpPolicyPushVersionRepository.incrementVersion();
        this.policiesPushVersion.incrementAndGet();
        this.refreshPolicies();
    }

    @RequestMapping(method = POST, value = {"/manage/parse"})
    public ResponseEntity<String> parsePolicyDefinition(@RequestBody PdpPolicyDefinition policyDefinition) {
        LOG.info("/manage/parse");
        String policyXml = policyTemplateEngine.createPolicyXml(policyDefinition);
        return ResponseEntity.ok(policyXml);
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
        try {
            lock.lock();
            LOG.info("Starting reloading policies");
            long start = System.currentTimeMillis();
            this.pdpEngine = pdpEngineHolder.newPdpEngine(cachePolicies, false);
            LOG.info("Finished reloading policies in {} ms", System.currentTimeMillis() - start);
        } finally {
            lock.unlock();
        }
    }

}
