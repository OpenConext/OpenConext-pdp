package pdp;

import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true)
public class PdpApplicationTest {

  @Value("${local.server.port}")
  private int port;
  private MultiValueMap<String, String> headers;
  private TestRestTemplate client = new TestRestTemplate("admin", "secret");

  @Before
  public void before() {
    headers = new LinkedMultiValueMap<>();
    headers.add("Content-Type", "application/json");
  }

  @Test
  public void test_permit() throws Exception {
    doDecide("Request.01.Permit.json", Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  @Test
  public void test_permit_with_categories_shorthand() throws Exception {
    doDecide("Request.01.Permit.CategoriesShorthand.json", Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  @Test
  public void test_indeterminate_no_policy_matches() throws Exception {
    doDecide("Request.01.NA.json", Decision.INDETERMINATE, "urn:oasis:names:tc:xacml:1.0:status:processing-error");
  }

  @Test
  public void test_deny() throws Exception {
    doDecide("Request.01.Deny.json", Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  private void doDecide(String requestJsonFile, Decision expectedDecision, String statusCodeValue) throws Exception {
    final String url = "http://localhost:" + port + "/decide";
    String json = IOUtils.toString(new ClassPathResource(requestJsonFile).getInputStream());
    HttpEntity<String> request = new HttpEntity<String>(json, headers);
    String s = client.postForObject(url, request, String.class);
    Response response = JSONResponse.load(s);
    assertEquals(1, response.getResults().size());
    Result result = response.getResults().iterator().next();
    assertEquals(expectedDecision, result.getDecision());
    assertEquals(statusCodeValue, result.getStatus().getStatusCode().getStatusCodeValue().getUri().toString());
  }

}
