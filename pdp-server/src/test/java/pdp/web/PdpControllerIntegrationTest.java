package pdp.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import pdp.AbstractPdpIntegrationTest;
import pdp.PdpApplication;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.xacml.PdpPolicyDefinitionParser;
import pdp.xacml.PolicyTemplateEngine;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static pdp.util.StreamUtils.singletonCollector;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = { "spring.profiles.active=no-csrf"})
public class PdpControllerIntegrationTest extends AbstractPdpIntegrationTest {

  private static final String policyId = "urn:surfconext:xacml:policy:id:_open_conextpdp_single_attribute";

  private PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();

  @Before
  @Override
  public void before() throws IOException {
    super.before();
    pdpPolicyViolationRepository.deleteAll();
  }

  @Test
  public void testPolicyDefinitions() throws Exception {
    String json = get("internal/policies");
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));
    // exact count depends on the ordering of the tests - does not really matter
    assertTrue(definitions.size() >= 9);
    definitions.forEach(def -> assertNotNull(def.getCreated()));
  }

  @Test
  public void testPolicyDefinitionsByServiceProvider() throws Exception {
    String json = get("internal/policies/sp?serviceProvider=https://surftest.viadesk.com");
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));
    assertEquals(1 , definitions.size());
  }

  @Test
  public void testViolations() throws Exception {
    setUpViolation(policyId);
    setUpViolation(policyId);
    String json = get("internal/violations");
    assertViolations(json, 2);
  }

  @Test
  public void testViolationsByPolicyId() throws Exception {
    Long policyId = setUpViolation(PdpControllerIntegrationTest.policyId).getId();
    String json = get("internal/violations/" + policyId);
    assertViolations(json, 1);
  }

  @Test
  public void testRevisionsByPolicyIdWithExistingPolicy() throws Exception {
    //we use the API to set up the revisions
    PdpPolicy policy = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).get(0);
    PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);
    String initialDenyAdvice = policyDefinition.getDenyAdvice();

    policyDefinition.setDenyAdvice("advice_changed");

    post("internal/policies", policyDefinition);

    policyDefinition.setDenyAdvice("advice_changed_again");
    post("internal/policies", policyDefinition);

    String json = get("internal/revisions/" + policy.getId());
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));

    assertEquals(3, definitions.size());
    assertEquals(initialDenyAdvice, findByRevisionNbr(definitions, 0).getDenyAdvice());
    assertEquals("advice_changed", findByRevisionNbr(definitions, 1).getDenyAdvice());
    assertEquals("advice_changed_again", findByRevisionNbr(definitions, 2).getDenyAdvice());

    PdpPolicy newRevision = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).get(0);
    assertEquals("advice_changed_again", pdpPolicyDefinitionParser.parse(newRevision).getDenyAdvice());

  }

  @Test
  public void testRevisionsByPolicyIdWithNewPolicy() throws Exception {
    PdpPolicy policy = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).get(0);
    PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);

    //this will ensure a new policy is created
    policyDefinition.setId(null);
    policyDefinition.setName("some name");

    post("internal/policies", policyDefinition);

    PdpPolicy saved = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(
        PolicyTemplateEngine.getPolicyId(policyDefinition.getName()), true).get(0);
    String json = get("internal/revisions/" + saved.getId());
    List<PdpPolicyDefinition> definitions = objectMapper.readValue(json, constructCollectionType(PdpPolicyDefinition.class));

    assertEquals(1, definitions.size());
    assertEquals(policyDefinition.getName(), definitions.get(0).getName());
  }


  @Test
  public void testDefaultPolicy() throws Exception {
    String json = get("internal/default-policy");
    PdpPolicyDefinition definition = objectMapper.readValue(json, PdpPolicyDefinition.class);
    assertFalse(definition.isAllAttributesMustMatch());
    assertFalse(definition.isDenyRule());
  }

  @Test
  public void testAllowedAttributes() throws Exception {
    List<JsonPolicyRequest.Attribute> allowedAttributes = objectMapper.readValue(get("internal/attributes"), constructCollectionType(JsonPolicyRequest.Attribute.class));
    assertEquals(8, allowedAttributes.size());
    assertAttributes(allowedAttributes);
  }

  @Test
  public void testAllowedSamlAttributes() throws Exception {
    List<JsonPolicyRequest.Attribute> samlAttributes = objectMapper.readValue(get("internal/saml-attributes"), constructCollectionType(JsonPolicyRequest.Attribute.class));
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
    PdpPolicy policy = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).get(0);
    PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);
    //verify that revisions are also deleted - so we create one
    post("internal/policies", policyDefinition);

    PdpPolicy latestRevision = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).get(0);
    int statusCode = restTemplate.exchange("http://localhost:" + port + "/pdp/api/internal/policies/" + latestRevision.getId(), HttpMethod.DELETE, new HttpEntity<>(headers), String.class).getStatusCode().value();
    assertEquals(200, statusCode);

    String json = get("internal/violations");
    assertViolations(json, 0);

    assertPolicyIsDeleted(policy);
    assertPolicyIsDeleted(latestRevision);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUser() throws Exception {
    Map<String, Object> shibbolethUser = objectMapper.readValue(get("internal/users/me"), Map.class);
    assertEquals("urn:collab:person:example.com:admin", shibbolethUser.get("username"));
    assertEquals("John Doe", shibbolethUser.get("displayName"));
    assertEquals("http://adfs2prod.aventus.nl/adfs/services/trust", shibbolethUser.get("authenticatingAuthority"));
    assertEquals(4, ArrayList.class.cast(shibbolethUser.get("idpEntities")).size());
    assertEquals(2, ArrayList.class.cast(shibbolethUser.get("spEntities")).size());
  }

  private PdpPolicyDefinition findByRevisionNbr(List<PdpPolicyDefinition> definitions, int revisionNbr) {
    return definitions.stream().filter(def -> def.getRevisionNbr() == revisionNbr).collect(singletonCollector());
  }

  private void assertPolicyIsDeleted(PdpPolicy policy) {
    try {
      get("/internal/policies/" + policy.getId());
      fail("Policy should not exists");
    } catch (HttpClientErrorException e) {
      assertEquals(404, e.getStatusCode().value());
    }
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

  private String get(String path) {
    return doExchange(path, new HttpEntity<>(headers), HttpMethod.GET);
  }

  private String post(String path, Object requestBody) throws JsonProcessingException {
    String jsonRequest = objectMapper.writeValueAsString(requestBody);
    return doExchange(path, new HttpEntity<>(jsonRequest, headers), HttpMethod.POST);
  }

  private String doExchange(String path, HttpEntity<?> requestEntity, HttpMethod method) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return restTemplate.exchange("http://localhost:" + port + "/pdp/api/" + path, method, requestEntity, String.class).getBody();
  }

  private PdpPolicy setUpViolation(String policyId) {
    PdpPolicy policy = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).get(0);
    pdpPolicyViolationRepository.save(new PdpPolicyViolation(policy, "json", "response", true));
    return policy;
  }


}