package pdp;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import pdp.domain.PdpPolicyDefinition;

import java.io.IOException;
import java.io.StringWriter;

public class PolicyTemplateEngine {

  private MustacheFactory mf;

  public static final String adviceIdPrefix = "urn:surfconext:xacml:advice:id:";

  public PolicyTemplateEngine() {
    mf = new DefaultMustacheFactory();
  }

  public String createPolicyXml(PdpPolicyDefinition pdpPolicyDefintion) {
    Mustache mustache = mf.compile("templates/policy-definition.xml");
    StringWriter writer = new StringWriter();
    try {
      mustache.execute(writer, pdpPolicyDefintion).flush();
      return writer.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
