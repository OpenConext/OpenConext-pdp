package pdp;

import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPEngineFactory;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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

  @Test
  public void testStandAlonePolicy() throws Exception {
    String payload = IOUtils.toString(new ClassPathResource("xacml/requests/test_request.json").getInputStream());
    Request pdpRequest = JSONRequest.load(payload);

    Response pdpResponse = pdpEngine.decide(pdpRequest);
    assertEquals(1, pdpResponse.getResults().size());

    Result result = pdpResponse.getResults().iterator().next();
    assertEquals(Decision.PERMIT, result.getDecision());
    assertEquals(1, result.getPolicyIdentifiers().size());
    assertEquals("http://axiomatics.com/alfa/identifier/OpenConext.pdp.IDPandGroupClause", result.getPolicyIdentifiers().iterator().next().getId().stringValue());
  }
}
