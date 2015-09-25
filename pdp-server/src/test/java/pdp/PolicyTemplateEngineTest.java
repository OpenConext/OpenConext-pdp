package pdp;

import org.junit.Before;
import org.junit.Test;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicyDefinition;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PolicyTemplateEngineTest {

  private PolicyTemplateEngine engine = new PolicyTemplateEngine();
  private PdpPolicyDefinitionParser parser = new PdpPolicyDefinitionParser();
  private PdpPolicyDefinition definition;

  @Before
  public void before() {
    definition = new PdpPolicyDefinition();
    definition.setName("Name Instelling");
    definition.setDescription("The long description");
    definition.setDenyAdvice("Sorry, no access");
    definition.setAttributes(Arrays.asList(new PdpAttribute("attr1", "value1"), new PdpAttribute("attr2", "value2")));
    definition.setIdentityProviderIds(Arrays.asList("http://mock-idp"));
    definition.setServiceProviderId("http://mock-sp");

  }

  @Test
  public void testTemplate() throws Exception {
    String policyXml = engine.createPolicyXml(definition);
    PdpPolicyDefinition fromPolicyXml = parser.parse(definition.getName(), policyXml);
    assertEquals(fromPolicyXml, definition);
  }
}
