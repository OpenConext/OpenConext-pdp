package pdp.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.std.json.JSONStructureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pdp.AbstractPdpIntegrationTest;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.policies.PolicyLoader;
import pdp.teams.VootClientConfig;
import pdp.xacml.PolicyTemplateEngine;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static pdp.util.StreamUtils.singletonCollector;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "no-csrf"})
public class PdpControllerShibbolethIntegrationTest extends AbstractPdpIntegrationTest {

    private static final ParameterizedTypeReference<List<PdpPolicyDefinition>> pdpPolicyDefinitionsType = new ParameterizedTypeReference<>() {
    };

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Before
    public void before() throws IOException {
        super.before();
        addShibHeaders();
    }

    @Test
    public void testPolicyDefinitionsImpersonated() {
        impersonate(PolicyLoader.authenticatingAuthority, VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN, "John Doe");

        List<PdpPolicyDefinition> definitions = getForObject("/internal/policies", pdpPolicyDefinitionsType);

        assertEquals(2, definitions.size());
    }

    @Test
    public void testInternalDecide() throws IOException, JSONStructureException {
        ResponseEntity<String> response = post("/internal/decide/policy", getJsonPolicyRequest());

        assertEquals(Decision.NOTAPPLICABLE, JSONResponse.load(response.getBody()).getResults().stream().collect(singletonCollector()).getDecision());
    }

    @Test
    public void testPolicyDefinitionsAdmin() {
        List<PdpPolicyDefinition> definitions = getForObject("/internal/policies", pdpPolicyDefinitionsType);

        assertThat(definitions, hasSize(greaterThan(2)));
    }

    @Test
    public void testPolicyDefinitionsByServiceProvider() {
        List<PdpPolicyDefinition> definitions = getForObject("/internal/policies/sp?serviceProvider=http://mock-sp", pdpPolicyDefinitionsType);

        assertThat(definitions, hasSize(1));
    }

    @Test
    public void testViolations() {
        setUpViolation(policyId);
        setUpViolation(policyId);
        List<PdpPolicyViolation> violations = getForObject("/internal/violations", new ParameterizedTypeReference<List<PdpPolicyViolation>>() {
        });

        assertViolations(violations, 2);
    }

    @Test
    public void testViolationsByPolicyId() {
        Long policyId = setUpViolation(PdpControllerShibbolethIntegrationTest.policyId).getId();
        List<PdpPolicyViolation> violations = getForObject("/internal/violations/" + policyId, new ParameterizedTypeReference<List<PdpPolicyViolation>>() {
        });

        assertViolations(violations, 1);
    }

    @Test
    public void findPolicyById() {
        PdpPolicy policy = getExistingPolicy();
        PdpPolicyDefinition definition = getForObject("/internal/policies/" + policy.getId(), new ParameterizedTypeReference<PdpPolicyDefinition>() {
        });

        assertNotNull(definition.getId());
    }

    @Test
    public void testRevisionsByPolicyIdWithExistingPolicy() {
        //we use the API to set up the revisions
        PdpPolicy policy = getExistingPolicy();
        PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);
        String initialDenyAdvice = policyDefinition.getDenyAdvice();
        policyDefinition.setDenyAdvice("advice_changed");
        Map<String, Object> map = objectMapper.convertValue(policyDefinition, new TypeReference<>() {
        });
        map.put("unknown", "value");
        assertThat(post("/internal/policies", map).getStatusCode(), is(HttpStatus.OK));

        policyDefinition.setDenyAdvice("advice_changed_again");
        assertThat(post("/internal/policies", policyDefinition).getStatusCode(), is(HttpStatus.OK));

        List<PdpPolicyDefinition> definitions = getForObject("/internal/revisions/" + policy.getId(), pdpPolicyDefinitionsType);

        assertThat(definitions, hasSize(3));
        assertEquals(initialDenyAdvice, findByRevisionNbr(definitions, 0).getDenyAdvice());
        assertEquals("advice_changed", findByRevisionNbr(definitions, 1).getDenyAdvice());
        assertEquals("advice_changed_again", findByRevisionNbr(definitions, 2).getDenyAdvice());

        PdpPolicy newRevision = getExistingPolicy();
        assertEquals("advice_changed_again", pdpPolicyDefinitionParser.parse(newRevision).getDenyAdvice());
    }

    @Test
    public void testRevisionsByPolicyIdWithNewPolicy() {
        PdpPolicy policy = getExistingPolicy();
        PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);

        // this will ensure a new policy is created
        policyDefinition.setId(null);
        policyDefinition.setName("some name");

        assertThat(post("/internal/policies", policyDefinition).getStatusCode(), is(HttpStatus.OK));

        PdpPolicy saved = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(
                PolicyTemplateEngine.getPolicyId(policyDefinition.getName()), true).get();

        List<PdpPolicyDefinition> definitions = getForObject("/internal/revisions/" + saved.getId(), pdpPolicyDefinitionsType);

        assertThat(definitions, hasSize(1));
        assertEquals(policyDefinition.getName(), definitions.get(0).getName());
    }

    @Test
    public void testDefaultPolicy() {
        PdpPolicyDefinition definition = getForObject("/internal/default-policy", new ParameterizedTypeReference<>() {
        });

        assertFalse(definition.isAllAttributesMustMatch());
        assertFalse(definition.isDenyRule());
    }

    @Test
    public void testAllowedAttributes() {
        List<JsonPolicyRequest.Attribute> allowedAttributes = getForObject("/internal/attributes", new ParameterizedTypeReference<>() {
        });

        assertThat(allowedAttributes, hasSize(9));
        assertAttributes(allowedAttributes);
    }

    @Test
    public void testAllowedSamlAttributes() {
        List<JsonPolicyRequest.Attribute> samlAttributes = getForObject("/internal/saml-attributes", new ParameterizedTypeReference<List<JsonPolicyRequest.Attribute>>() {
        });

        assertThat(samlAttributes, hasSize(11));

        JsonPolicyRequest.Attribute nameId = samlAttributes.get(0);

        assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", nameId.attributeId);
        assertNotNull(nameId.value);
        assertAttributes(samlAttributes);
    }

    @Test
    public void testDeletePdpPolicy() {
        // verify that violations are also deleted - so we create one
        setUpViolation(policyIdToDelete);

        PdpPolicy policy = getExistingPolicy(policyIdToDelete);
        PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(policy);

        // verify that revisions are also deleted - so we create one
        assertThat(post("/internal/policies", policyDefinition).getStatusCode(), is(HttpStatus.OK));

        PdpPolicy latestRevision = getExistingPolicy(policyIdToDelete);

        assertThat(delete("/internal/policies/" + latestRevision.getId()).getStatusCode(), is(HttpStatus.OK));

        List<PdpPolicyViolation> violations = getForObject("/internal/violations", new ParameterizedTypeReference<List<PdpPolicyViolation>>() {
        });
        assertViolations(violations, 0);
        assertPolicyIsDeleted(policy);
        assertPolicyIsDeleted(latestRevision);
    }

    @Test
    public void testUser() {
        Map<String, Object> shibbolethUser = getForObject("/internal/users/me", new ParameterizedTypeReference<Map<String, Object>>() {
        });

        assertEquals("urn:collab:person:example.com:admin", shibbolethUser.get("username"));
        assertEquals("John Doe", shibbolethUser.get("displayName"));
        assertEquals(PolicyLoader.authenticatingAuthority, shibbolethUser.get("authenticatingAuthority"));

        assertEquals(1, ((Collection) shibbolethUser.get("idpEntities")).size());
        assertEquals(1, ((Collection) shibbolethUser.get("spEntities")).size());
    }

    @Test
    public void testCreatePolicyWithNameError() throws JsonParseException, JsonMappingException, IOException {
        PdpPolicyDefinition policyDefinition = pdpPolicyDefinitionParser.parse(getExistingPolicy());
        policyDefinition.setId(null);

        ResponseEntity<String> response = post("/internal/policies", policyDefinition);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> map = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {
        });
        String message = (String) ((Map<?, ?>) map.get("details")).get("name");

        assertTrue(message.contains("Policy name must be unique"));
    }

    @Test
    public void testAssertBadRequestUnknownIdentityProvider() {
        PdpPolicyDefinition policyDefinition = getPdpPolicyDefinitionFromExistingPolicy();

        impersonate("http://xxx-idp", "urn:collab:person:example.com:mary.doe", "Mary Doe");

        ResponseEntity<String> post = post("/internal/policies", policyDefinition);

        assertEquals(HttpStatus.BAD_REQUEST, post.getStatusCode());
        assertTrue(post.getBody().contains("http://xxx-idp is not a valid or known IdP / SP entityId"));
    }

    @Test
    public void testConflicts() {
        ResponseEntity<String> response = get("/internal/conflicts");

        assertNotNull(response.getBody());
    }

    @Override
    public TestRestTemplate getRestTemplate() {
        return restTemplate;
    }

    private PdpPolicyDefinition findByRevisionNbr(List<PdpPolicyDefinition> definitions, int revisionNbr) {
        return definitions.stream().filter(def -> def.getRevisionNbr() == revisionNbr).collect(singletonCollector());
    }

    private void assertViolations(List<PdpPolicyViolation> violations, int expectedSize) {
        assertThat(violations, hasSize(expectedSize));
        violations.forEach(v -> assertNotNull(v.getCreated()));
    }

    private void assertAttributes(List<JsonPolicyRequest.Attribute> allowedAttributes) {
        allowedAttributes.forEach(attr -> {
            assertTrue(attr.attributeId.startsWith("urn"));
            assertNotNull(attr.value);
        });
    }

}