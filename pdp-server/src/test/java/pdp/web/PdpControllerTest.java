package pdp.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import pdp.AbstractPdpIntegrationTest;
import pdp.PdpApplication;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@WebIntegrationTest(randomPort = true, value = {"xacml.properties.path=classpath:xacml.conext.properties", "spring.profiles.active=no-csrf"})
public class PdpControllerTest extends AbstractPdpIntegrationTest {

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
    assertEquals(9, definitions.size());
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
    Long policyId = setUpViolation(PdpControllerTest.policyId).getId();
    String json = get("internal/violations/" + policyId);
    assertViolations(json, 1);
  }

  @Test
  public void testRevisionsByPolicyId() throws Exception {
    //we use the API to set up the revisions
    PdpPolicy policy = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).get(0);
    PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);
    String initialDenyAdvice = policyDefinition.getDenyAdvice();

    policyDefinition.setDenyAdvice("advice_changed");
    //this will ensure one revision is made
    policyDefinition.setId(policy.getId());

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
  public void testDefaultPolicy() throws Exception {

  }

  @Test
  public void testAllowedAttributes() throws Exception {

  }

  @Test
  public void testAllowedSamlAttributes() throws Exception {

  }

  @Test
  public void testCreatePdpPolicy() throws Exception {

  }

  @Test
  public void testDeletePdpPolicy() throws Exception {

  }

  @Test
  public void testUser() throws Exception {

  }

  private PdpPolicyDefinition findByRevisionNbr(List<PdpPolicyDefinition> definitions, int revisionNbr) {
    return definitions.stream().filter(def -> def.getRevisionNbr() == revisionNbr).collect(PdpApplication.singletonCollector());
  }

  private void assertViolations(String json, int expectedSize) throws IOException {
    List<PdpPolicyViolation> violations = objectMapper.readValue(json, constructCollectionType(PdpPolicyViolation.class));
    assertEquals(expectedSize, violations.size());
    violations.forEach(v -> assertTrue(v.getCreated().before(new Date())));
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