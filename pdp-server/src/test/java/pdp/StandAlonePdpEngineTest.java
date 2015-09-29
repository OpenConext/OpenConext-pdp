package pdp;

import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.api.*;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPEngineFactory;
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
import pdp.xacml.ClassPathPolicyFinderFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StandAlonePdpEngineTest extends AbstractXacmlTest {

  private static Logger LOG = LoggerFactory.getLogger(PdpController.class);

  private PDPEngine pdpEngine;

  @Before
  public void before() throws IOException, FactoryException {
    Resource resource = new ClassPathResource("test.standalone.engine.xacml.properties");
    String absolutePath = resource.getFile().getAbsolutePath();

    //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
    System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

    PDPEngineFactory pdpEngineFactory = PDPEngineFactory.newInstance();
    this.pdpEngine = pdpEngineFactory.newEngine();
  }

  @After
  public void after() throws Exception {
    super.after();
    System.setProperty(ClassPathPolicyFinderFactory.POLICY_FILES, "not to be found");
  }

  @Test
  public void testMultiplaAndPolicy() throws Exception {
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
  public void testConflictingPolicies() throws Exception {
    Result result = doDecideTest("test_request_conflicting_polices.json", Decision.DENY,
        "OpenConext.pdp.test.conflicting.policies.1.Policy.xml",
        "OpenConext.pdp.test.conflicting.policies.2.Policy.xml"
    );
    Optional<IdReference> policy = result.getPolicyIdentifiers().stream().collect(PdpApplication.singletonOptionalCollector());
    assertTrue(policy.isPresent());
    //the violated policy
    assertEquals("OpenConext.pdp.test.conflicting.policies.2.Policy.xml", policy.get().getId().stringValue());
  }

  private Result doDecideTest(final String requestFile, Decision decision, String... policyFiles) throws Exception {
    //Policies file will be picked up by ClassPathPolicyFinderFactory
    System.setProperty(ClassPathPolicyFinderFactory.POLICY_FILES, String.join(",", Arrays.asList(policyFiles)));

    String payload = IOUtils.toString(new ClassPathResource("xacml/requests/" + requestFile).getInputStream());
    Request pdpRequest = JSONRequest.load(payload);

    Response pdpResponse = pdpEngine.decide(pdpRequest);

    LOG.debug(JSONResponse.toString(pdpResponse, true));

    assertEquals(1, pdpResponse.getResults().size());

    Result result = pdpResponse.getResults().iterator().next();
    assertEquals(decision, result.getDecision());

    return result;
  }

}
