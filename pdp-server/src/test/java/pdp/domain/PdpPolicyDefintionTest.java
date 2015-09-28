package pdp.domain;

import org.junit.Test;
import pdp.AbstractXacmlTest;
import pdp.PolicyTemplateEngine;
import pdp.xacml.DevelopmentPrePolicyLoader;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class PdpPolicyDefintionTest extends AbstractXacmlTest {

  private final PolicyTemplateEngine templateEngine = new PolicyTemplateEngine();
  private final PdpPolicyDefinitionParser policyDefinitionParser = new PdpPolicyDefinitionParser();

  @Test
  public void fromPolicyXml() throws Exception {
    /*
     * We need to check if we construct a PdpPolicyDefinition based on a XML policy and then re-create
     * the XML policy from the PdpPolicyDefinition using the PolicyTemplateEngine and then revert it
     * back to the PdPPolicyDefinition again the output is the same as the input
     */
    List<PdpPolicy> policies = new DevelopmentPrePolicyLoader().getPolicies();
    List<PdpPolicyDefinition> input = policies.stream().map(policy -> policyDefinitionParser.parse(policy.getName(), policy.getPolicyXml())).collect(toList());
    List<PdpPolicyDefinition> output = input.stream().map(definition -> policyDefinitionParser.parse(definition.getName(), templateEngine.createPolicyXml(definition))).collect(toList());
    /*
     * This is redundant but if there are differences between the PdpPolicyDefinition's then the List comparison is un-readable
     */
    input.forEach(def -> {
      assertEquals(
          def.getName(),
          def,
          output.stream().filter(outputDef -> outputDef.getName().equals(def.getName())).findFirst().get());
    });
    assertEquals(input, output);

  }

}