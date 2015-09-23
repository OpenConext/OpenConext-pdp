package pdp;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class PolicyTemplateEngineTest {

  @Test
  public void testSimpleTemplate() throws IOException {
    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache mustache = mf.compile("templates/policy-definition.xml");
    StringWriter writer = new StringWriter();
    PdpPolicyDefintion defintion = new PdpPolicyDefintion();
    defintion.setName("name Nice Instelling");
    defintion.setDescription("description-nice");
    defintion.setDenyAdvice("deny-advice-nice");
    defintion.setAttributes(Arrays.asList(new PdpAttribute("attr1", "value1"), new PdpAttribute("attr2", "value2")));
    defintion.setIdentityProviderId("idp-nice");
    defintion.setServiceProviderId("sp-id");
    mustache.execute(writer, defintion).flush();
    System.out.println(writer.toString());

    //todo

    //load this policy and see of everything is correct
  }
}
