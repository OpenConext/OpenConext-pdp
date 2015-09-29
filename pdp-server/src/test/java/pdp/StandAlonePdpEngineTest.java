package pdp;

import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPEngineFactory;
import org.apache.openaz.xacml.api.pdp.PDPException;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONStructureException;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pdp.xacml.ClassPathPolicyFinderFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StandAlonePdpEngineTest extends AbstractXacmlTest {

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
    System.setProperty(ClassPathPolicyFinderFactory.POLICY_LOCATION_FILE_KEY, "not to be found");
  }

  @Test
  public void testMultiplaAndPolicy() throws Exception {
    doDecideTest("OpenConext.pdp.test.multiple.and.Policy.xml", "test_request_multiple_and.json", Decision.PERMIT);
  }

  @Test
  public void testDenyPolicyWithPermit() throws Exception {
    doDecideTest("OpenConext.pdp.test.deny.Policy.xml", "test_request_deny_policy_permit.json", Decision.PERMIT);
  }

  @Test
  public void testDenyPolicyWithDeny() throws Exception {
    doDecideTest("OpenConext.pdp.test.deny.Policy.xml", "test_request_deny_policy_deny.json", Decision.DENY);
  }

  @Test
  public void testDenyPolicyWithMissingAttribute() throws Exception {
    Result result = doDecideTest("OpenConext.pdp.test.deny.Policy.xml", "test_request_deny_policy_missing_attribute.json", Decision.INDETERMINATE);
    assertEquals("Missing required attribute", result.getStatus().getStatusMessage());
  }

  @Test
  public void testDenyPolicyWithNoPolicyFound() throws Exception {
    Result result = doDecideTest("OpenConext.pdp.test.deny.Policy.xml", "test_request_no_matching_target.json", Decision.NOTAPPLICABLE);
    assertEquals("No matching root policy found", result.getStatus().getStatusMessage());
  }

  private Result doDecideTest(String policyFile, final String requestFile, Decision decision) throws IOException, JSONStructureException, PDPException {
    //Policy file is lazily loaded by standard XACML implementation and will be picked up by ClassPathPolicyFinderFactory
    System.setProperty(ClassPathPolicyFinderFactory.POLICY_LOCATION_FILE_KEY, "xacml/test-policies/" + policyFile);

    String payload = IOUtils.toString(new ClassPathResource("xacml/requests/" + requestFile).getInputStream());
    Request pdpRequest = JSONRequest.load(payload);

    Response pdpResponse = pdpEngine.decide(pdpRequest);
    assertEquals(1, pdpResponse.getResults().size());

    Result result = pdpResponse.getResults().iterator().next();

    assertEquals(decision, result.getDecision());

    return result;
  }

}
