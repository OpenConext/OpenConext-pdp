package pdp.web;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import pdp.AbstractPdpIntegrationTest;
import pdp.PdpApplication;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.policies.PolicyLoader;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pdp.access.FederatedUserBuilder.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = { "spring.profiles.active=dev"})
public class PdpApiControllerIntegrationTest extends AbstractPdpIntegrationTest {

  @Before
  @Override
  public void before() throws IOException {
    super.before();
    headers.set(X_IDP_ENTITY_ID, PolicyLoader.authenticatingAuthority);
    headers.set(X_UNSPECIFIED_NAME_ID, "urn:collab:person:example.com:mary.doe");
    headers.set(X_DISPLAY_NAME, "Mary Doe");
  }

  @Test
  public void testAssertBadRequestUnknownIdentityProvider() throws Exception {
    PdpPolicyDefinition policyDefinition = getPdpPolicyDefinitionFromExistingPolicy();

    headers.set(X_IDP_ENTITY_ID, "http://xxx-idp");

    ResponseEntity<String> post = post("internal/policies", policyDefinition);

    assertEquals(HttpStatus.BAD_REQUEST, post.getStatusCode());
    assertTrue(post.getBody().contains("http://xxx-idp is not a valid or known IdP / SP entityId"));
  }

  @Override
  public RestTemplate getRestTemplate() {
    return testRestTemplate;
  }



}