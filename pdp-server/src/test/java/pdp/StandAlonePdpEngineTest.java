package pdp;

import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.api.Attribute;
import org.apache.openaz.xacml.api.AttributeCategory;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.IdReference;
import org.apache.openaz.xacml.api.Obligation;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pdp.domain.PdpPolicy;
import pdp.policies.PolicyLoader;
import pdp.repositories.PdpPolicyRepository;
import pdp.sab.SabClient;
import pdp.sab.SabClientConfig;
import pdp.sab.SabPIP;
import pdp.teams.TeamsPIP;
import pdp.teams.VootClient;
import pdp.teams.VootClientConfig;
import pdp.web.PdpController;
import pdp.xacml.OpenConextPDPEngineFactory;
import pdp.xacml.PolicyTemplateEngine;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pdp.util.StreamUtils.singletonCollector;

@NotThreadSafe
public class StandAlonePdpEngineTest extends AbstractXacmlTest {

    private static Logger LOG = LoggerFactory.getLogger(PdpController.class);

    private PDPEngine pdpEngine;

    private PdpPolicyRepository pdpPolicyRepository;

    private static VootClient vootClient = new VootClientConfig().mockVootClient();

    private static SabClient sabClient = new SabClientConfig().mockSabClient("user", "password", "http://localhost");

    @BeforeClass
    public static void beforeClass() throws IOException {
        Resource resource = new ClassPathResource("xacml.conext.properties");
        String absolutePath = resource.getFile().getAbsolutePath();

        //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
        System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

        XACMLProperties.reloadProperties();
    }


    private void setUp(String... policyFiles) throws IOException, FactoryException {
        pdpPolicyRepository = mock(PdpPolicyRepository.class);
        List<PdpPolicy> pdpPolicies = Arrays.asList(policyFiles).stream().map(policyFile -> loadPolicy(policyFile)).collect(toList());
        when(pdpPolicyRepository.findAll()).thenReturn(pdpPolicies);

        OpenConextPDPEngineFactory pdpEngineFactory = new OpenConextPDPEngineFactory();
        this.pdpEngine = pdpEngineFactory.newEngine(true, false, pdpPolicyRepository, vootClient, sabClient);
    }

    private PdpPolicy loadPolicy(String policyFile) {
        try {
            String policyXml = IOUtils.toString(new ClassPathResource("xacml/test-policies/" + policyFile).getInputStream(), "UTF-8");
            return new PdpPolicy(policyXml, policyFile, true, "system", PolicyLoader.authenticatingAuthority, "John Doe", true, "reg");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testMultipleAndPolicy() throws Exception {
        doDecideTest("test_request_multiple_and.json", Decision.PERMIT, "OpenConext.pdp.test.multiple.and.Policy.xml");
    }

    @Test
    public void testDenyPolicyWithPermit() throws Exception {
        doDecideTest("test_request_deny_policy_permit.json", Decision.PERMIT, "OpenConext.pdp.test.deny.Policy.xml");
    }

    @Test
    public void testMulivaluedAttributePolicyWithPermit() throws Exception {
        doDecideTest("test_request_multivalued_attribute.json", Decision.PERMIT, "OpenConext.pdp.test.multivalued.attribute.Policy.xml");
    }

    @Test
    public void testDenyPolicyWithDeny() throws Exception {
        doDecideTest("test_request_deny_policy_deny.json", Decision.DENY, "OpenConext.pdp.test.deny.Policy.xml");
    }

    @Test
    public void testDenyPolicyWithMissingAttribute() throws Exception {
        Result result = doDecideTest("test_request_deny_policy_missing_attribute.json", Decision.INDETERMINATE, "OpenConext.pdp.test.deny.Policy.xml");
        assertEquals("Missing required attribute", result.getStatus().getStatusMessage());
    }

    @Test
    public void testDenyPolicyWithNoPolicyFound() throws Exception {
        doDecideTest("test_request_no_matching_target.json", Decision.NOTAPPLICABLE, "OpenConext.pdp.test.deny.Policy.xml");
    }

    @Test
    public void testPermitPolicyWithMultipleOr() throws Exception {
        doDecideTest("test_request_multiple_or.json", Decision.PERMIT, "OpenConext.pdp.test.multiple.or.Policy.xml");
    }

    @Test
    public void testTeamsPolicyWithAggregatedAttributes() throws Exception {
        Result result = doDecideTest("test_request_teams_policy.json", Decision.PERMIT, "OpenConext.pdp.test.teams.Policy.xml");
        assertAggregatedAttribute(result, TeamsPIP.GROUP_URN, "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo");
    }

    @Test
    public void testSabPolicyWithAggregatedAttributes() throws Exception {
        Result result = doDecideTest("test_request_sab_policy.json", Decision.PERMIT, "OpenConext.pdp.test.sab.Policy.xml");
        assertAggregatedAttribute(result, SabPIP.SAB_URN, "OperationeelBeheerder");
    }

    @Test
    public void testPermitPolicyWithMultipleIdp() throws Exception {
        doDecideTest("test_request_multiple_or.json", Decision.PERMIT, "OpenConext.pdp.test.multiple.or.Policy.xml");
    }

    @Test
    public void testConflictingPolicies() throws Exception {
        Result result = doDecideTest("test_request_conflicting_policies.json", Decision.DENY,
            "OpenConext.pdp.test.conflicting.policies.1.Policy.xml",
            "OpenConext.pdp.test.conflicting.policies.2.Policy.xml"
        );
        IdReference reference = result.getPolicyIdentifiers().stream().collect(singletonCollector());
        //the violated policy
        assertEquals(PolicyTemplateEngine.getPolicyId("OpenConext.pdp.test.conflicting.policies.2.Policy.xml"), reference.getId().stringValue());
    }

    @Test
    public void testNotApplicable() throws Exception {
        doDecideTest("json_policy_request.json", Decision.NOTAPPLICABLE, "OpenConext.pdp.test.unknown.SP.Policy.xml");
    }

    @Test
    public void testIpRangeObligation() throws Exception {
        Result result = doDecideTest("json_policy_ip_range_request.json", Decision.PERMIT, "OpenConext.pdp.test.obligations.Policy.xml");
        Collection<Obligation> obligations = result.getObligations();

        assertEquals(1, obligations.size());

        Obligation obligation = obligations.iterator().next();
        assertEquals("http://test2.surfconext.nl/assurance/loa2",
            obligation.getAttributeAssignments().iterator().next().getAttributeValue().getValue());
    }

    @Test
    public void testIpRangeMultipleRulesObligation() throws Exception {
        Result result = doDecideTest("json_policy_ip_range_request.json", Decision.PERMIT,
            "OpenConext.pdp.test.multiple.rules.obligations.Policy.xml");
        Collection<Obligation> obligations = result.getObligations();

        assertEquals(1, obligations.size());

        Obligation obligation = obligations.iterator().next();
        assertEquals("http://test2.surfconext.nl/assurance/loa3",
            obligation.getAttributeAssignments().iterator().next().getAttributeValue().getValue());
    }

    @Test
    public void testIpRangeObligationNegate() throws Exception {
        Result result = doDecideTest("json_policy_ip_range_request.json", Decision.PERMIT,
            "OpenConext.pdp.test.obligations.negate.Policy.xml");
        Collection<Obligation> obligations = result.getObligations();

        assertEquals(0, obligations.size());
    }

    @Test
    public void testIpOutOfRangeObligation() throws Exception {
        Result result = doDecideTest("json_policy_ip_not_in_range_request.json", Decision.PERMIT, "OpenConext.pdp.test.obligations.Policy.xml");
        Collection<Obligation> obligations = result.getObligations();

        assertEquals(0, obligations.size());
    }

    @Test
    public void testIpRangeObligationNotApplicable() throws Exception {
        doDecideTest("json_policy_ip_range_not_applicable_request.json", Decision.NOTAPPLICABLE, "OpenConext.pdp.test.obligations.Policy.xml");
    }

    @Test
    public void testIpRangeObligationIndeterminate() throws Exception {
        doDecideTest("json_policy_ip_range_indeterminate_request.json", Decision.INDETERMINATE, "OpenConext.pdp.test.obligations.Policy.xml");
    }

    @Test
    public void multipleObligationsCombined() throws Exception {
        Result result = doDecideTest("json_policy_ip_range_request.json", Decision.PERMIT,
            "OpenConext.pdp.test.obligations.Policy.xml",
            "OpenConext.pdp.test.obligations.loa3.Policy.xml");
        Collection<Obligation> obligations = result.getObligations();

        assertEquals(2, obligations.size());

        Obligation obligation = obligations.iterator().next();
        assertEquals("http://test2.surfconext.nl/assurance/loa2",
            obligation.getAttributeAssignments().iterator().next().getAttributeValue().getValue());

    }

    @Test
    public void testIpRangeAndAffiliationObligation() throws Exception {
        Result result = doDecideTest("json_policy_ip_range_and_affiliation_request.json",
            Decision.PERMIT, "OpenConext.pdp.test.obligations.multiple.and.Policy.xml");
        Collection<Obligation> obligations = result.getObligations();

        assertEquals(1, obligations.size());

        Obligation obligation = obligations.iterator().next();
        assertEquals("http://test2.surfconext.nl/assurance/loa3",
            obligation.getAttributeAssignments().iterator().next().getAttributeValue().getValue());
    }

    @Test
    public void testStepupTemplate() throws Exception {
        Result result = doDecideTest("json_policy_stepup_request.json",
            Decision.PERMIT, "stepup.policy.template.xml");
        Collection<Obligation> obligations = result.getObligations();

        assertEquals(1, obligations.size());

        Obligation obligation = obligations.iterator().next();
        assertEquals("http://localhost/assurance/loa3",
            obligation.getAttributeAssignments().iterator().next().getAttributeValue().getValue());
    }

    private Result doDecideTest(final String requestFile, Decision decision, String... policyFiles) throws Exception {
        setUp(policyFiles);

        String payload = IOUtils.toString(new ClassPathResource("xacml/requests/" + requestFile).getInputStream(), "UTF-8");
        Request pdpRequest = JSONRequest.load(payload);

        Response pdpResponse = pdpEngine.decide(pdpRequest);
        return assertResponse(decision, pdpResponse);
    }

    private Result assertResponse(Decision decision, Response pdpResponse) throws Exception {
        String json = JSONResponse.toString(pdpResponse, true);

        if (decision.equals(Decision.DENY)) {
            assertTrue(json.contains("\"AttributeId\" : \"IdPOnly\""));
        }

        assertEquals(1, pdpResponse.getResults().size());

        Result result = pdpResponse.getResults().iterator().next();
        assertEquals(decision, result.getDecision());
        return result;
    }

    private void assertAggregatedAttribute(Result result, String urn, String attributeValue) {
        List<Attribute> attributes = result.getAttributes().stream().map(AttributeCategory::getAttributes).flatMap(Collection::stream).collect(toList());
        assertEquals(1, attributes.size());
        Attribute attribute = attributes.get(0);
        assertEquals(urn, attribute.getAttributeId().getUri().toString());
        assertEquals(urn, attribute.getCategory().getUri().toString());
        List<String> attributeValues = attribute.getValues().stream().map(attrValue -> (String) attrValue.getValue()).collect(toList());
        assertEquals(Arrays.asList(attributeValue), attributeValues);
    }

}
