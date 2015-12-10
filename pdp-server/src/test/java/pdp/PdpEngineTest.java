package pdp;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import pdp.domain.*;
import pdp.teams.TeamsPIP;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.junit.Assert.*;
import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;
import static pdp.xacml.PdpPolicyDefinitionParser.*;

/**
 * Note this class is slow. it starts up the entire Spring boot app.
 * <p/>
 * If you want to test policies quickly then see StandAlonePdpEngineTest
 */
public class PdpEngineTest extends AbstractPdpIntegrationTest {

  @Test
  public void testAllPolicies() throws Exception {
    JsonPolicyRequest policyRequest = getJsonPolicyRequest();
    List<PdpPolicy> policies = policyLoader.getPolicies();

    //lambda is poor with error handling
    for (PdpPolicy policy: policies) {
      doTestPolicy(policyRequest, policy);
    }
  }

  @Test
  public void testCrsfConfiguration() throws Exception {
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(getJsonPolicyRequest()), headers);
    Map jsonResponse = testRestTemplate.postForObject("http://localhost:" + port + "/pdp/api/internal/decide/policy", request, Map.class);
    assertEquals(403, jsonResponse.get("status"));
    assertEquals("Expected CSRF token not found. Has your session expired?", jsonResponse.get("message"));
  }

  private void doTestPolicy(JsonPolicyRequest policyRequest, PdpPolicy policy) throws Exception {
    PdpPolicyDefinition definition = policyDefinitionParser.parse(policy);

    JsonPolicyRequest permitPolicyRequest = policyRequest.copy();

    // We don't want INDETERMINATE Decisions based on the Target of the policy so we ensure the SP and  - optional - IDP is set on the JSON request
    permitPolicyRequest.addOrReplaceResourceAttribute(SP_ENTITY_ID, definition.getServiceProviderId());
    if (definition.getIdentityProviderIds().isEmpty()) {
      permitPolicyRequest.deleteAttribute(IDP_ENTITY_ID);
    } else {
      permitPolicyRequest.addOrReplaceResourceAttribute(IDP_ENTITY_ID, definition.getIdentityProviderIds().get(0));
    }
    JsonPolicyRequest denyPolicyRequest = permitPolicyRequest.copy();
    JsonPolicyRequest denyIndeterminatePolicyRequest = permitPolicyRequest.copy();

    Set<Map.Entry<String, List<PdpAttribute>>> entries = definition.getAttributes().stream().collect(Collectors.groupingBy(PdpAttribute::getName)).entrySet();
    entries.forEach(entry -> {
      //The permitPolicyRequest is mangled so every required attribute from the Policy is included
      permitPolicyRequest.addOrReplaceAccessSubjectAttribute(entry.getKey(), entry.getValue().get(0).getValue());
      denyPolicyRequest.addOrReplaceAccessSubjectAttribute(entry.getKey(), "will-not-match-" + UUID.randomUUID().toString());
      denyIndeterminatePolicyRequest.deleteAttribute(entry.getKey());
    });

    //See VootClientConfig#mockVootClient for groups to be returned and VootClientConfig#URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN for the user it expects
    if (definition.getAttributes().stream().anyMatch(attr -> attr.getName().equalsIgnoreCase(TeamsPIP.GROUP_URN))) {
      permitPolicyRequest.addOrReplaceAccessSubjectAttribute(NAME_ID, URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN);
    }

    JsonPolicyRequest notApplicablePolicyRequest = permitPolicyRequest.copy();
    notApplicablePolicyRequest.addOrReplaceResourceAttribute(SP_ENTITY_ID, UUID.randomUUID().toString());
    notApplicablePolicyRequest.addOrReplaceResourceAttribute(IDP_ENTITY_ID, UUID.randomUUID().toString());

    //We can't use Transactional rollback as the Application runs in a different process.
    postDecide(policy, permitPolicyRequest, definition.isDenyRule() ? Decision.DENY : Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
    postDecide(policy, denyPolicyRequest, definition.isDenyRule() ? Decision.PERMIT : Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
    postDecide(policy, denyIndeterminatePolicyRequest,
        definition.isDenyRule() ? Decision.INDETERMINATE : Decision.DENY,
        definition.isDenyRule() ? "urn:oasis:names:tc:xacml:1.0:status:missing-attribute" : "urn:oasis:names:tc:xacml:1.0:status:ok");
    postDecide(policy, notApplicablePolicyRequest, Decision.NOTAPPLICABLE, "urn:oasis:names:tc:xacml:1.0:status:ok");

    assertViolations(policy.getPolicyId());
  }

  private void postDecide(PdpPolicy policy, JsonPolicyRequest policyRequest, Decision expectedDecision, String statusCodeValue) throws Exception {
    final String url = "http://localhost:" + port + "/pdp/api/decide/policy";
    String jsonRequest = objectMapper.writeValueAsString(policyRequest);
    HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);
    String jsonResponse = testRestTemplate.postForObject(url, request, String.class);
    Response response = JSONResponse.load(jsonResponse);
    assertEquals(policy.getName(), 1, response.getResults().size());
    Result result = response.getResults().iterator().next();
    assertEquals(policy.getName(), expectedDecision, result.getDecision());
    assertEquals(policy.getName(), statusCodeValue, result.getStatus().getStatusCode().getStatusCodeValue().getUri().toString());
  }

  private void assertViolations(String policyId) throws Exception {
    List<PdpPolicyViolation> violations = stream(pdpPolicyViolationRepository.findAll().spliterator(), false).filter(violation -> violation.getPolicy().getPolicyId().equals(policyId)).collect(toList());
    assertFalse(CollectionUtils.isEmpty(violations));
    violations.forEach(violation -> assertTrue(isValid(violation)));
  }

  private boolean isValid(PdpPolicyViolation violation) {
    return violation.getPolicy() != null
        && StringUtils.hasText(violation.getJsonRequest())
        && StringUtils.hasText(violation.getResponse());
  }

}
