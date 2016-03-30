package pdp.web;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.std.json.JSONStructureException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import pdp.AbstractPdpIntegrationTest;
import pdp.PdpApplication;
import pdp.policies.PolicyLoader;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static pdp.util.StreamUtils.singletonCollector;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=no-csrf"})
public class PdpControllerBasicAuthenticationIntegrationTest extends AbstractPdpIntegrationTest {

  private String PASSWORD = "secret";
  private String PDP_USER = "pdp-admin";

  private RestTemplate restTemplate;

  @After
  public void clear() {
    this.restTemplate = null;
  }

  @Test
  public void shouldBeAbleToFetchPoliciesWithBasciAuthAndHeadersOnProtectedEndPoint() {
    basicAuthTemplate();
    impersonate(PolicyLoader.authenticatingAuthority, "urn:collab:person:example.com:mary.doe", "Mary Doe");

    ResponseEntity<String> response = get("/protected/policies");

    assertThat(response.getStatusCode(), is(OK));
  }

  @Test
  public void shouldNotBeAbleToFetchPoliciesWithBasciAuthAndNoHeadersOnProtectedEndPoint() {
    basicAuthTemplate();
    ResponseEntity<String> response = get("/protected/policies");

    assertThat(response.getStatusCode(), is(FORBIDDEN));
  }

  @Test
  public void shouldNotBeAbleToCallProtectedEndPointWithShibHeaders() {
    noAuthTemplate();
    addShibHeaders();

    ResponseEntity<String> response = get("/protected/policies");

    assertThat(response.getStatusCode(), is(FORBIDDEN));
  }

  @Test
  public void optionsShouldShouldAtLeastContainGetMethod() {
    basicAuthTemplate();
    impersonate(PolicyLoader.authenticatingAuthority, "urn:collab:person:example.com:mary.doe", "Mary Doe");

    Set<HttpMethod> options = options("/protected/policies");

    assertThat(options, hasItem(HttpMethod.GET));
  }

  @Override
  public RestTemplate getRestTemplate() {
    return restTemplate;
  }

  private void basicAuthTemplate() {
    this.restTemplate = new TestRestTemplate(PDP_USER, PASSWORD);
  }

  private void noAuthTemplate() {
    this.restTemplate = new TestRestTemplate();
  }

}