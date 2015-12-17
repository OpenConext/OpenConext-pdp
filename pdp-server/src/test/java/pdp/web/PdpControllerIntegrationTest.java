package pdp.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import pdp.AbstractPdpIntegrationTest;
import pdp.PdpApplication;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.policies.PolicyLoader;
import pdp.xacml.PolicyTemplateEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static pdp.util.StreamUtils.singletonCollector;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=no-csrf"})
public class PdpControllerIntegrationTest extends AbstractPdpIntegrationTest {

  @Before
  @Override
  public void before() throws IOException {
    super.before();
    pdpPolicyViolationRepository.deleteAll();
  }

  @Test
  public void testPolicyDefinitionsImpersonated() throws Exception {
    String json = getImpersonated("internal/policies", PolicyLoader.authenticatingAuthority).getBody();
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));
    // only one, the policy with no IdP and a SP with allowedAll
    assertEquals(1, definitions.size());
    assertEquals("google.com/a/terenatest.org", definitions.get(0).getServiceProviderId());
  }

  @Test
  public void testPolicyDefinitionsAdmin() throws Exception {
    String json = get("internal/policies").getBody();
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));
    // all of the policies
    assertTrue( definitions.size() >= 9);
  }

  @Test
  public void testPolicyDefinitionsByServiceProvider() throws Exception {
    String json = get("internal/policies/sp?serviceProvider=https://surftest.viadesk.com").getBody();
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));
    assertEquals(1, definitions.size());
  }

  @Test
  public void testViolations() throws Exception {
    setUpViolation(policyId);
    setUpViolation(policyId);
    String json = get("internal/violations").getBody();
    assertViolations(json, 2);
  }

  @Test
  public void testViolationsByPolicyId() throws Exception {
    Long policyId = setUpViolation(PdpControllerIntegrationTest.policyId).getId();
    String json = get("internal/violations/" + policyId).getBody();
    assertViolations(json, 1);
  }

  @Test
  public void testRevisionsByPolicyIdWithExistingPolicy() throws Exception {
    //we use the API to set up the revisions
    PdpPolicy policy = getExistingPolicy();
    PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);
    String initialDenyAdvice = policyDefinition.getDenyAdvice();

    policyDefinition.setDenyAdvice("advice_changed");

    post("internal/policies", policyDefinition);

    policyDefinition.setDenyAdvice("advice_changed_again");
    post("internal/policies", policyDefinition);

    String json = get("internal/revisions/" + policy.getId()).getBody();
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));

    assertEquals(3, definitions.size());
    assertEquals(initialDenyAdvice, findByRevisionNbr(definitions, 0).getDenyAdvice());
    assertEquals("advice_changed", findByRevisionNbr(definitions, 1).getDenyAdvice());
    assertEquals("advice_changed_again", findByRevisionNbr(definitions, 2).getDenyAdvice());

    PdpPolicy newRevision = getExistingPolicy();
    assertEquals("advice_changed_again", pdpPolicyDefinitionParser.parse(newRevision).getDenyAdvice());

  }

  @Test
  public void testRevisionsByPolicyIdWithNewPolicy() throws Exception {
    PdpPolicy policy = getExistingPolicy();
    PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);

    //this will ensure a new policy is created
    policyDefinition.setId(null);
    policyDefinition.setName("some name");

    post("internal/policies", policyDefinition);

    PdpPolicy saved = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(
        PolicyTemplateEngine.getPolicyId(policyDefinition.getName()), true).get(0);
    String json = get("internal/revisions/" + saved.getId()).getBody();
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));

    assertEquals(1, definitions.size());
    assertEquals(policyDefinition.getName(), definitions.get(0).getName());
  }


  @Test
  public void testDefaultPolicy() throws Exception {
    String json = get("internal/default-policy").getBody();
    PdpPolicyDefinition definition = objectMapper.readValue(json, PdpPolicyDefinition.class);
    assertFalse(definition.isAllAttributesMustMatch());
    assertFalse(definition.isDenyRule());
  }

  @Test
  public void testAllowedAttributes() throws Exception {
    List<JsonPolicyRequest.Attribute> allowedAttributes = objectMapper.readValue(get("internal/attributes").getBody(), constructCollectionType(JsonPolicyRequest.Attribute.class));
    assertEquals(8, allowedAttributes.size());
    assertAttributes(allowedAttributes);
  }

  @Test
  public void testAllowedSamlAttributes() throws Exception {
    List<JsonPolicyRequest.Attribute> samlAttributes = objectMapper.readValue(get("internal/saml-attributes").getBody(), constructCollectionType(JsonPolicyRequest.Attribute.class));
    assertEquals(9, samlAttributes.size());
    JsonPolicyRequest.Attribute nameId = samlAttributes.get(0);
    assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", nameId.attributeId);
    assertNotNull(nameId.value);
    assertAttributes(samlAttributes);
  }

  @Test
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

    String json = get("internal/violations").getBody();
    assertViolations(json, 0);

    assertPolicyIsDeleted(policy);
    assertPolicyIsDeleted(latestRevision);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUser() throws Exception {
    Map<String, Object> shibbolethUser = objectMapper.readValue(get("internal/users/me").getBody(), Map.class);
    assertEquals("urn:collab:person:example.com:admin", shibbolethUser.get("username"));
    assertEquals("John Doe", shibbolethUser.get("displayName"));
    assertEquals(PolicyLoader.authenticatingAuthority, shibbolethUser.get("authenticatingAuthority"));
    assertEquals(1, ArrayList.class.cast(shibbolethUser.get("idpEntities")).size());
    assertEquals(1, ArrayList.class.cast(shibbolethUser.get("spEntities")).size());
  }


  @Override
  public RestTemplate getRestTemplate() {
    return restTemplate;
  }

  private PdpPolicyDefinition findByRevisionNbr(List<PdpPolicyDefinition> definitions, int revisionNbr) {
    return definitions.stream().filter(def -> def.getRevisionNbr() == revisionNbr).collect(singletonCollector());
  }

  private void assertViolations(String json, int expectedSize) throws IOException {
    List<PdpPolicyViolation> violations = objectMapper.readValue(json, constructCollectionType(PdpPolicyViolation.class));
    assertEquals(expectedSize, violations.size());
    violations.forEach(v -> assertTrue(v.getCreated().before(new Date())));
  }

  private void assertAttributes(List<JsonPolicyRequest.Attribute> allowedAttributes) {
    allowedAttributes.forEach(attr -> {
      assertTrue(attr.attributeId.startsWith("urn"));
      assertNotNull(attr.value);
    });
  }

}