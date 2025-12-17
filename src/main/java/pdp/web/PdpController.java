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
import pdp.repositories.PdpPolicyPushVersionRepository;
import pdp.repositories.PdpPolicyRepository;
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
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static pdp.util.StreamUtils.singletonCollector;
import static pdp.util.StreamUtils.singletonOptionalCollector;

@RestController
@RequestMapping(
    value = {"/pdp/api"},
    headers = {"Content-Type=application/json"},
    produces = {"application/json"})
public class PdpController implements JsonMapper {

    private final static Logger LOG = LoggerFactory.getLogger(PdpController.class);

    private final PDPEngineHolder pdpEngineHolder;
    private final PdpPolicyRepository pdpPolicyRepository;
    private final PolicyTemplateEngine policyTemplateEngine = new PolicyTemplateEngine();
    private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();

    private final ReentrantLock lock = new ReentrantLock();
    private final PdpPolicyPushVersionRepository pdpPolicyPushVersionRepository;

    // Can't be final as we need to swap this reference for reloading policies in production
    private volatile PDPEngine pdpEngine;
    private final AtomicLong policiesPushVersion = new AtomicLong();

    @Autowired
    public PdpController(PdpPolicyRepository pdpPolicyRepository,
                         PDPEngineHolder pdpEngineHolder,
                         PdpPolicyPushVersionRepository pdpPolicyPushVersionRepository) {
        this.pdpEngineHolder = pdpEngineHolder;
        this.pdpEngine = pdpEngineHolder.newPdpEngine(false);
        this.pdpPolicyRepository = pdpPolicyRepository;
        this.pdpPolicyPushVersionRepository = pdpPolicyPushVersionRepository;
        this.policiesPushVersion.set(pdpPolicyPushVersionRepository.findById(1L).get().getVersion());

    }

    @RequestMapping(method = RequestMethod.POST, value = "/decide/policy")
    public String decide(@RequestBody String payload) throws Exception {
        Long currentDatabasePoliciesPushVersion = this.pdpPolicyPushVersionRepository.getCurrentVersion();
        if (currentDatabasePoliciesPushVersion != this.policiesPushVersion.get()) {
            this.refreshPolicies(false);
        }
        return doDecide(payload);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/manage/decide")
    public String decideManage(@RequestBody String payload) throws Exception {
        return decide(payload);
    }

    private String doDecide(String payload) throws Exception {
        StatsContext stats = StatsContextHolder.getContext();

        long start = System.currentTimeMillis();
        LOG.debug("decide request: {}", payload);

        Request request = JSONRequest.load(payload);
        addStatsDetails(stats, request);

        returnPolicyIdInList(request);
        Response pdpResponse;
        lock.lock();
        try {
            pdpResponse = pdpEngine.decide(request);
        } finally {
            lock.unlock();
        }

        String response = JSONResponse.toString(pdpResponse, LOG.isDebugEnabled());

        long took = System.currentTimeMillis() - start;
        stats.setResponseTimeMs(took);
        LOG.debug("decide response: {} took: {} ms", response, took);

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

        LOG.info("/manage/push saved policies:" + policies.size());

        //Delete all first to prevent finding out any delta between existing and new policies
        pdpPolicyRepository.deleteAll();
        pdpPolicyRepository.saveAll(policies);
        this.refreshPolicies(true);
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
        stats.setServiceProvider(getAttributeValue(attributes, "SPentityID").orElse(""));
    }

    private Optional<String> getAttributeValue(Collection<Attribute> attributes, String attributeId) {
        Optional<Attribute> attribute = attributes.stream().filter(attr -> attr.getAttributeId().getUri().toString().equals(attributeId)).collect(singletonOptionalCollector());
        return attribute.map(attr -> (String) attr.getValues().iterator().next().getValue());
    }

    private Optional<String> getOptionalLoa(Response pdpResponse) {
        return pdpResponse.getResults().stream().flatMap(result -> result.getObligations().stream()
                .map(obligation -> obligation.getAttributeAssignments().stream()
                    .map(attributeAssignment -> String.class.cast(attributeAssignment.getAttributeValue().getValue()))))
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

    private void refreshPolicies(boolean incrementDatabase) {
        LOG.info("Starting reloading policies with increment database {}", incrementDatabase);
        long start = System.currentTimeMillis();

        lock.lock();
        try {
            // Increment the database counter so other nodes will refresh also
            if (incrementDatabase) {
                this.pdpPolicyPushVersionRepository.incrementVersion();
            }

            Long newDatabasePoliciesPushVersion = this.pdpPolicyPushVersionRepository.getCurrentVersion();
            // Set with the current database counter to prevent an endless loop of refreshes
            LOG.info("Updating new DB policy push version, old memory value {}, new value database {}",
                this.policiesPushVersion.get(), newDatabasePoliciesPushVersion);

            this.policiesPushVersion.set(newDatabasePoliciesPushVersion);
            //this will cause a reload of all policies
            this.pdpEngine = pdpEngineHolder.newPdpEngine(false);
            LOG.info("Finished reloading policies in {} ms", System.currentTimeMillis() - start);
        } finally {
            lock.unlock();
        }
    }

}
