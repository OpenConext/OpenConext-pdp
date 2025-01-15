package pdp.xacml;

import org.junit.Test;
import pdp.AbstractXacmlTest;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.policies.PolicyLoader;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PolicyTemplateEngineTest extends AbstractXacmlTest {

    private final PolicyTemplateEngine engine = new PolicyTemplateEngine();
    private final PdpPolicyDefinitionParser parser = new PdpPolicyDefinitionParser();

    private PdpPolicyDefinition policyDefinition() {
        PdpPolicyDefinition definition = new PdpPolicyDefinition();
        definition.setId("1");
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
        return definition;
    }

    @Test
    public void testTemplateWithLogicalOr() throws Exception {
        PdpPolicyDefinition definition = this.policyDefinition();
        assertEquality(definition);
    }

    @Test
    public void testTemplateWithLogicalAnd() throws Exception {
        PdpPolicyDefinition definition = this.policyDefinition();
        definition.setAllAttributesMustMatch(true);
        assertEquality(definition);
    }

    @Test
    public void testTemplateWithDenyRule() throws Exception {
        PdpPolicyDefinition definition = this.policyDefinition();
        definition.setDenyRule(true);
        assertEquality(definition);
    }

    @Test
    public void testGetPolicyId() {
        String name = "A very invalid $^%#@* ''policy NAME\"1";
        String policyId = PolicyTemplateEngine.getPolicyId(name);
        assertEquals("urn:surfconext:xacml:policy:id:a_very_invalid_policy_name_1", policyId);
    }

    private void assertEquality(PdpPolicyDefinition policyDefinition) {
        String policyXml = engine.createPolicyXml(policyDefinition);
        PdpPolicy policy = new PdpPolicy(policyXml, policyDefinition.getName(), true, "system",
            "http://mock-ipd", "John Doe", true, "reg");

        PdpPolicyDefinition fromPolicyXml = parser.parse(policy);
        assertEquals(fromPolicyXml, policyDefinition);
    }
}
