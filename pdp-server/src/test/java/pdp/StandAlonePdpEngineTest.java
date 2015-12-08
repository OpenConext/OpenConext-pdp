package pdp;

import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.api.*;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPEngineFactory;
import org.apache.openaz.xacml.pdp.policy.Policy;
import org.apache.openaz.xacml.pdp.policy.PolicyDef;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.repositories.PdpPolicyRepository;
import pdp.teams.VootClient;
import pdp.teams.VootClientConfig;
import pdp.web.PdpController;
import pdp.xacml.OpenConextPDPEngine;
import pdp.xacml.OpenConextPDPEngineFactory;
import pdp.xacml.PdpPolicyDefinitionParser;
import pdp.xacml.PolicyTemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pdp.xacml.ClassPathPolicyFinderFactory.*;

public class StandAlonePdpEngineTest extends AbstractXacmlTest {

  private static Logger LOG = LoggerFactory.getLogger(PdpController.class);

  private PDPEngine pdpEngine;

  private PdpPolicyRepository pdpPolicyRepository;

  private VootClient mockVootClient = new VootClient(null, null) {
    @Override
    @SuppressWarnings("ignoreChecked")
    public List<String> groups(String userUrn) {
      return VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN.equals(userUrn) ?
          Collections.singletonList("urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo") : Collections.EMPTY_LIST;
    }
  };

  private void setUp(String... policyFiles) throws IOException, FactoryException {
    Resource resource = new ClassPathResource("xacml.conext.properties");
    String absolutePath = resource.getFile().getAbsolutePath();

    //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
    System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

    XACMLProperties.reloadProperties();

    pdpPolicyRepository = mock(PdpPolicyRepository.class);
    List<PdpPolicy> pdpPolicies = Arrays.asList(policyFiles).stream().map(policyFile -> loadPolicy(policyFile)).collect(toList());
    when(pdpPolicyRepository.findAll()).thenReturn(pdpPolicies);

    OpenConextPDPEngineFactory pdpEngineFactory = new OpenConextPDPEngineFactory();
    this.pdpEngine = pdpEngineFactory.newEngine(true, pdpPolicyRepository, mockVootClient);
  }

  private PdpPolicy loadPolicy(String policyFile)  {
    try {
      String policyXml = IOUtils.toString(new ClassPathResource("xacml/test-policies/" + policyFile).getInputStream());
      return new PdpPolicy( policyXml, policyFile, true, "system", "http://mock-idp", "John Doe");
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
  public void testTeamsPolicy() throws Exception {
    doDecideTest("test_request_teams_policy.json", Decision.PERMIT, "OpenConext.pdp.test.teams.Policy.xml");
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
    Optional<IdReference> policy = result.getPolicyIdentifiers().stream().collect(PdpApplication.singletonOptionalCollector());
    assertTrue(policy.isPresent());
    //the violated policy
    assertEquals(PolicyTemplateEngine.getPolicyId("OpenConext.pdp.test.conflicting.policies.2.Policy.xml"), policy.get().getId().stringValue());
  }

  private Result doDecideTest(final String requestFile, Decision decision, String... policyFiles) throws Exception {
    setUp(policyFiles);

    String payload = IOUtils.toString(new ClassPathResource("xacml/requests/" + requestFile).getInputStream());
    Request pdpRequest = JSONRequest.load(payload);

    Response pdpResponse = pdpEngine.decide(pdpRequest);
    assertResponse(decision, pdpResponse);

    pdpResponse = pdpEngine.decide(pdpRequest);
    Result result = assertResponse(decision, pdpResponse);

    return result;
  }

  private Result assertResponse(Decision decision, Response pdpResponse) throws Exception {
    LOG.debug(JSONResponse.toString(pdpResponse, true));

    assertEquals(1, pdpResponse.getResults().size());

    Result result = pdpResponse.getResults().iterator().next();
    assertEquals(decision, result.getDecision());
    return result;
  }

}
