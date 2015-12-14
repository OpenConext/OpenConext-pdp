package pdp;

import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.api.*;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;
import pdp.sab.SabClient;
import pdp.sab.SabPIP;
import pdp.teams.TeamsPIP;
import pdp.teams.VootClient;
import pdp.teams.VootClientConfig;
import pdp.web.PdpController;
import pdp.xacml.OpenConextPDPEngineFactory;
import pdp.xacml.PolicyTemplateEngine;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pdp.util.StreamUtils.singletonOptionalCollector;

@NotThreadSafe
public class StandAlonePdpEngineTest extends AbstractXacmlTest {

  private static Logger LOG = LoggerFactory.getLogger(PdpController.class);

  private PDPEngine pdpEngine;

  private PdpPolicyRepository pdpPolicyRepository;

  private static VootClient mockVootClient = new VootClient(null, null) {
    @Override
    @SuppressWarnings("ignoreChecked")
    public List<String> groups(String userUrn) {
      return VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN.equals(userUrn) ?
          Arrays.asList("urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo",
              "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:admins") : Collections.EMPTY_LIST;
    }
  };

  private static SabClient mockSabClient = new SabClient("user", "password", "http://localhost") {
    @Override
    @SuppressWarnings("ignoreChecked")
    public List<String> roles(String userUrn) {
      return VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN.equals(userUrn) ?
          Arrays.asList("OperationeelBeheerder", "Instellingsbevoegde") : Collections.EMPTY_LIST;
    }
  };

  private void setUp(boolean includeAggregatedAttributesInResponse, String... policyFiles) throws IOException, FactoryException {
    Resource resource = new ClassPathResource("xacml.conext.properties");
    String absolutePath = resource.getFile().getAbsolutePath();

    //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
    System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

    XACMLProperties.reloadProperties();

    pdpPolicyRepository = mock(PdpPolicyRepository.class);
    List<PdpPolicy> pdpPolicies = Arrays.asList(policyFiles).stream().map(policyFile -> loadPolicy(policyFile)).collect(toList());
    when(pdpPolicyRepository.findAll()).thenReturn(pdpPolicies);

    OpenConextPDPEngineFactory pdpEngineFactory = new OpenConextPDPEngineFactory();
    this.pdpEngine = pdpEngineFactory.newEngine(includeAggregatedAttributesInResponse, pdpPolicyRepository, mockVootClient, mockSabClient);
  }

  private PdpPolicy loadPolicy(String policyFile) {
    try {
      String policyXml = IOUtils.toString(new ClassPathResource("xacml/test-policies/" + policyFile).getInputStream());
      return new PdpPolicy(policyXml, policyFile, true, "system", "http://mock-idp", "John Doe", true);
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
    doDecideTest("test_request_multiple_and.json", Decision.PERMIT, true, "OpenConext.pdp.test.multiple.and.Policy.xml");
  }

  @Test
  public void testDenyPolicyWithPermit() throws Exception {
    doDecideTest("test_request_deny_policy_permit.json", Decision.PERMIT, true, "OpenConext.pdp.test.deny.Policy.xml");
  }

  @Test
  public void testDenyPolicyWithDeny() throws Exception {
    doDecideTest("test_request_deny_policy_deny.json", Decision.DENY, true, "OpenConext.pdp.test.deny.Policy.xml");
  }

  @Test
  public void testDenyPolicyWithMissingAttribute() throws Exception {
    Result result = doDecideTest("test_request_deny_policy_missing_attribute.json", Decision.INDETERMINATE, true, "OpenConext.pdp.test.deny.Policy.xml");
    assertEquals("Missing required attribute", result.getStatus().getStatusMessage());
  }

  @Test
  public void testDenyPolicyWithNoPolicyFound() throws Exception {
    doDecideTest("test_request_no_matching_target.json", Decision.NOTAPPLICABLE, true, "OpenConext.pdp.test.deny.Policy.xml");
  }

  @Test
  public void testPermitPolicyWithMultipleOr() throws Exception {
    doDecideTest("test_request_multiple_or.json", Decision.PERMIT, true, "OpenConext.pdp.test.multiple.or.Policy.xml");
  }

  @Test

  public void testTeamsPolicyWithAggregatedAttributes() throws Exception {
    Result result = doDecideTest("test_request_teams_policy.json", Decision.PERMIT, true, "OpenConext.pdp.test.teams.Policy.xml");
    assertAggregatedAttribute(result, TeamsPIP.GROUP_URN, "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:admins");
  }

  @Test
  public void testTeamsPolicyWithoutAggregatedAttributes() throws Exception {
    Result result = doDecideTest("test_request_teams_policy.json", Decision.PERMIT, false, "OpenConext.pdp.test.teams.Policy.xml");
    assertEquals(0, result.getAttributes().size());
  }

  @Test
  public void testSabPolicyWithAggregatedAttributes() throws Exception {
    Result result = doDecideTest("test_request_sab_policy.json", Decision.PERMIT, true, "OpenConext.pdp.test.sab.Policy.xml");
    assertAggregatedAttribute(result, SabPIP.SAB_URN, "OperationeelBeheerder");
  }

  @Test
  public void testSabPolicyWithoutAggregatedAttributes() throws Exception {
    Result result = doDecideTest("test_request_sab_policy.json", Decision.PERMIT, false, "OpenConext.pdp.test.sab.Policy.xml");
    assertEquals(0, result.getAttributes().size());
  }

  @Test
  public void testPermitPolicyWithMultipleIdp() throws Exception {
    doDecideTest("test_request_multiple_or.json", Decision.PERMIT, true, "OpenConext.pdp.test.multiple.or.Policy.xml");
  }

  @Test
  public void testConflictingPolicies() throws Exception {
    Result result = doDecideTest("test_request_conflicting_policies.json", Decision.DENY, true,
        "OpenConext.pdp.test.conflicting.policies.1.Policy.xml",
        "OpenConext.pdp.test.conflicting.policies.2.Policy.xml"
    );
    Optional<IdReference> policy = result.getPolicyIdentifiers().stream().collect(singletonOptionalCollector());
    assertTrue(policy.isPresent());
    //the violated policy
    assertEquals(PolicyTemplateEngine.getPolicyId("OpenConext.pdp.test.conflicting.policies.2.Policy.xml"), policy.get().getId().stringValue());
  }

  private Result doDecideTest(final String requestFile, Decision decision, boolean includeAggregatedAttributesInResponse, String... policyFiles) throws Exception {
      setUp(includeAggregatedAttributesInResponse, policyFiles);

      String payload = IOUtils.toString(new ClassPathResource("xacml/requests/" + requestFile).getInputStream());
      Request pdpRequest = JSONRequest.load(payload);

      Response pdpResponse = pdpEngine.decide(pdpRequest);
      return assertResponse(decision, pdpResponse);
  }

  private Result assertResponse(Decision decision, Response pdpResponse) throws Exception {
    LOG.debug(JSONResponse.toString(pdpResponse, true));

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
