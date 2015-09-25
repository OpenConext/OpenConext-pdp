package pdp.domain;

import org.apache.openaz.xacml.util.XACMLProperties;
import org.junit.After;
import org.junit.Test;
import pdp.PolicyTemplateEngine;
import pdp.xacml.DevelopmentPrePolicyLoader;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class PdpPolicyDefintionTest {

  private PolicyTemplateEngine templateEngine = new PolicyTemplateEngine();

  @After
  public void after() throws Exception {
    /*
     * There is only one single static instance of XACML properties and as we don't provide one here
     * other tests fail to set the properties file as the default initialization is cached
     */
    XACMLProperties.reloadProperties();
  }

  @Test
  public void fromPolicyXml() throws Exception {
    /*
     * We need to check if we construct a PdpPolicyDefinition based on a XML policy and then re-create
     * the XML policy from the PdpPolicyDefinition using the PolicyTemplateEngine and then revert it
     * back to the PdPPolicyDefinition again the output is the same as the input
     */
    PdpPolicyDefinitionParser parser = new PdpPolicyDefinitionParser();
    List<PdpPolicy> policies = new DevelopmentPrePolicyLoader().getPolicies();
    List<PdpPolicyDefinition> input = policies.stream().map(policy -> parser.parse(policy.getName(), policy.getPolicyXml())).collect(toList());
    List<PdpPolicyDefinition> output = input.stream().map(definition -> parser.parse(definition.getName(), templateEngine.createPolicyXml(definition))).collect(toList());
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