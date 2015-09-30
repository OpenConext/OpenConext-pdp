package pdp;

import org.junit.Before;
import org.junit.Test;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicyDefinition;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PolicyTemplateEngineTest extends AbstractXacmlTest {

  private PolicyTemplateEngine engine = new PolicyTemplateEngine();
  private PdpPolicyDefinitionParser parser = new PdpPolicyDefinitionParser();
  private PdpPolicyDefinition definition;

  @Before
  public void before() {
    definition = new PdpPolicyDefinition();
    definition.setName("Name Instelling");
    definition.setDescription("The long description");
    definition.setDenyAdvice("Sorry, no access");
    definition.setAttributes(Arrays.asList(
        new PdpAttribute("attr1", "value1"),
        new PdpAttribute("attr1", "value1a"),
        new PdpAttribute("attr2", "value2")));
    definition.setIdentityProviderIds(Arrays.asList("http://mock-idp", "http://mock-ipd2"));
    definition.setServiceProviderId("http://mock-sp");
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

  private void assertEquality() {
    String policyXml = engine.createPolicyXml(definition);
    PdpPolicyDefinition fromPolicyXml = parser.parse(definition.getName(), policyXml);
    assertEquals(fromPolicyXml, definition);
  }
}
