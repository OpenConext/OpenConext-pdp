package pdp.xacml;

import org.junit.Before;
import org.junit.Test;
import pdp.AbstractXacmlTest;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.policies.PolicyLoader;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PolicyTemplateEngineTest extends AbstractXacmlTest {

    private PolicyTemplateEngine engine = new PolicyTemplateEngine();
    private PdpPolicyDefinitionParser parser = new PdpPolicyDefinitionParser();
    private PdpPolicyDefinition definition;

    @Before
    public void before() {
        definition = new PdpPolicyDefinition();
        definition.setId(1L);
        definition.setName("Name Instelling");
        definition.setDescription("The long description");
        definition.setDenyAdvice("Sorry, no access");
        definition.setDenyAdviceNl("Sorry, geen toegang");
        definition.setAttributes(Arrays.asList(
            new PdpAttribute("attr1", "value1"),
            new PdpAttribute("attr1", "value1a"),
            new PdpAttribute("attr2", "value2")));
        definition.setIdentityProviderIds(Arrays.asList(PolicyLoader.authenticatingAuthority, "http://mock-ipd2"));
        definition.setServiceProviderIds(Arrays.asList("http://mock-sp"));
        definition.setType("reg");
    }

    @Test
    public void testTemplateWithLogicalOr() throws Exception {
        assertEquality();
    }

    @Test
    public void testTemplateWithLogicalAnd() throws Exception {
        definition.setAllAttributesMustMatch(true);
        assertEquality();
    }

    @Test
    public void testTemplateWithDenyRule() throws Exception {
        definition.setDenyRule(true);
        assertEquality();
    }

    @Test
    public void testGetPolicyId() {
        String name = "A very invalid $^%#@* ''policy NAME\"1";
        String policyId = PolicyTemplateEngine.getPolicyId(name);
        assertEquals("urn:surfconext:xacml:policy:id:a_very_invalid_policy_name_1", policyId);
    }

    private void assertEquality() {
        String policyXml = engine.createPolicyXml(definition);
        PdpPolicy policy = new PdpPolicy(policyXml, definition.getName(), true, "system",
            "http://mock-ipd", "John Doe", true, "reg");

        PdpPolicyDefinition fromPolicyXml = parser.parse(policy);
        assertEquals(fromPolicyXml, definition);
    }
}
