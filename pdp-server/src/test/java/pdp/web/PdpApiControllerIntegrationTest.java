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

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pdp.access.PolicyIdpAccessEnforcerFilter.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = { "spring.profiles.active=dev"})
public class PdpApiControllerIntegrationTest extends AbstractPdpIntegrationTest {

  @Before
  @Override
  public void before() throws IOException {
    super.before();
    headers.set(X_IDP_ENTITY_ID, "http://mock-idp");
    headers.set(X_UNSPECIFIED_NAME_ID, "urn:collab:person:example.com:mary.doe");
    headers.set(X_DISPLAY_NAME, "Mary Doe");
  }

  @Test
  public void testAssertBadRequestUnknownIdentityProvider() throws Exception {
    PdpPolicyDefinition policyDefinition = getPdpPolicyDefinitionFromExistingPolicy();

    headers.set(X_IDP_ENTITY_ID, "http://xxx-idp");

    ResponseEntity<String> post = post("internal/policies", policyDefinition);

    assertEquals(HttpStatus.BAD_REQUEST, post.getStatusCode());
    assertTrue(post.getBody().contains("http://xxx-idp is not a valid or known IdentityProvider entityId"));
  }

  @Ignore(value = "Work in progress to get the correct access......")
  @Test
  public void testCreatePolicy() throws Exception {
    PdpPolicyDefinition policyDefinition = getPdpPolicyDefinitionFromExistingPolicy();
    policyDefinition.setDenyAdvice("advice_changed");

    headers.set(X_IDP_ENTITY_ID, policyDefinition.getIdentityProviderIds().get(0));

    ResponseEntity<String> post = post("internal/policies", policyDefinition);

    PdpPolicy newRevision = getExistingPolicy();
    assertEquals("advice_changed", pdpPolicyDefinitionParser.parse(newRevision).getDenyAdvice());

  }
  @Test
  @Ignore
  public void testDeletePdpPolicy() throws Exception {
    //verify that violations are also deleted - so we create one
    setUpViolation(policyId);
    PdpPolicy policy = getExistingPolicy();
    PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);
    //verify that revisions are also deleted - so we create one
    post("internal/policies", policyDefinition);

    PdpPolicy latestRevision = getExistingPolicy();
    int statusCode = restTemplate.exchange("http://localhost:" + port + "/pdp/api/internal/policies/" + latestRevision.getId(), HttpMethod.DELETE, new HttpEntity<>(headers), String.class).getStatusCode().value();
    assertEquals(200, statusCode);

    assertPolicyIsDeleted(policy);
    assertPolicyIsDeleted(latestRevision);
  }


  @Override
  public RestTemplate getRestTemplate() {
    return testRestTemplate;
  }



}