package pdp;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;
import pdp.domain.*;
import pdp.policies.PolicyLoader;
import pdp.teams.TeamsPIP;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;
import static pdp.xacml.PdpPolicyDefinitionParser.*;

/**
 * Note this class is slow. it starts up the entire Spring boot app.
 * <p>
 * If you want to test policies quickly then see StandAlonePdpEngineTest
 */
@ActiveProfiles("dev")
public class PdpEngineTest extends AbstractPdpIntegrationTest {

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @Ignore
    public void testAllPolicies() throws Exception {
        JsonPolicyRequest policyRequest = getJsonPolicyRequest();
        List<PdpPolicy> policies = policyLoader.getPolicies();

        //lambda's are poor with error handling as the stacktrace gets lost
        for (PdpPolicy policy : policies) {
            doTestPolicy(policyRequest, policy);
        }
    }

    @Override
    public TestRestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * Test for the policy all possible scenario's mangling the JSON request
     */
    private void doTestPolicy(JsonPolicyRequest policyRequest, PdpPolicy policy) throws Exception {
        PdpPolicyDefinition definition = policyDefinitionParser.parse(policy);

        JsonPolicyRequest permitPolicyRequest = policyRequest.copy();

        // We don't want INDETERMINATE Decisions based on the Target of the policy so we ensure the SP and  - optional - IDP is set on the JSON request
        permitPolicyRequest.addOrReplaceResourceAttribute(SP_ENTITY_ID, definition.getServiceProviderIds().get(0));
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

        becomeAnApiClientSoWeDontNeedACSRFToken();

        // We can't use Transactional rollback as the Application runs in a different process.
        postDecide(policy, permitPolicyRequest, definition.isDenyRule() ? Decision.DENY : Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
        postDecide(policy, denyPolicyRequest, definition.isDenyRule() ? Decision.PERMIT : Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
        postDecide(policy, denyIndeterminatePolicyRequest,
                definition.isDenyRule() ? Decision.INDETERMINATE : Decision.DENY,
                definition.isDenyRule() ? "urn:oasis:names:tc:xacml:1.0:status:missing-attribute" : "urn:oasis:names:tc:xacml:1.0:status:ok");
        postDecide(policy, notApplicablePolicyRequest, Decision.NOTAPPLICABLE, "urn:oasis:names:tc:xacml:1.0:status:ok");
    }

    private void becomeAnApiClientSoWeDontNeedACSRFToken() {
        restTemplate = new TestRestTemplate("pdp_admin", "secret");
    }

    private void postDecide(PdpPolicy policy, JsonPolicyRequest policyRequest, Decision expectedDecision, String statusCodeValue) throws Exception {
        String jsonResponse = post("/decide/policy", policyRequest).getBody();

        Response response = JSONResponse.load(jsonResponse);

        assertThat(policy.getName(), response.getResults(), hasSize(1));
        Result result = response.getResults().iterator().next();
        assertEquals(policy.getName(), expectedDecision, result.getDecision());
        assertEquals(policy.getName(), statusCodeValue, result.getStatus().getStatusCode().getStatusCodeValue().getUri().toString());
    }

    private boolean isValid(PdpPolicyViolation violation) {
        return violation.getPolicy() != null
                && StringUtils.hasText(violation.getJsonRequest())
                && StringUtils.hasText(violation.getResponse());
    }

}
