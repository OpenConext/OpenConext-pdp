package pdp.web;

import static org.junit.Assert.assertEquals;
import static pdp.util.StreamUtils.singletonCollector;

import java.io.IOException;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.std.json.JSONStructureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import pdp.AbstractPdpIntegrationTest;
import pdp.PdpApplication;
import pdp.policies.PolicyLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=dev"})
public class PdpApiControllerIntegrationTest extends AbstractPdpIntegrationTest {

  @Override
  public RestTemplate getRestTemplate() {
    return testRestTemplate;
  }

  @Test
  public void testInternalDecide() throws IOException, JSONStructureException {
    impersonate(PolicyLoader.authenticatingAuthority, "urn:collab:person:example.com:mary.doe", "Mary Doe");

    ResponseEntity<String> response = post("internal/decide/policy", getJsonPolicyRequest());

    assertEquals(Decision.NOTAPPLICABLE, JSONResponse.load(response.getBody()).getResults().stream().collect(singletonCollector()).getDecision());
  }

}