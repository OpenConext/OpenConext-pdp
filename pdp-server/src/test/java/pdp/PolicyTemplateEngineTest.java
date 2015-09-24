package pdp;

import org.junit.Before;
import org.junit.Test;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicyDefintion;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PolicyTemplateEngineTest {

  private PolicyTemplateEngine engine = new PolicyTemplateEngine();
  private PdpPolicyDefintion defintion;

  @Before
  public void before() {
    defintion = new PdpPolicyDefintion();
    defintion.setName("Name Instelling");
    defintion.setDescription("The long description");
    defintion.setDenyAdvice("Sorry, no access");
    defintion.setAttributes(Arrays.asList(new PdpAttribute("attr1", "value1"), new PdpAttribute("attr2", "value2")));
    defintion.setIdentityProviderId("http://mock-idp");
    defintion.setServiceProviderId("http://mock-sp");

  }

  @Test
  public void testTemplate() throws Exception {
    String policyXml = engine.createPolicyXml(defintion);
    PdpPolicyDefintion fromPolicyXml = new PdpPolicyDefintion(defintion.getName(), policyXml);
    assertEquals(fromPolicyXml, defintion);
  }
}
