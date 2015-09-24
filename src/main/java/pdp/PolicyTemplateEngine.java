package pdp;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.openaz.xacml.pdp.policy.PolicyDef;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicyDefintion;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

public class PolicyTemplateEngine {

  private MustacheFactory mf;

  public static final String adviceIdPrefix = "urn:surfconext:xacml:advice:id:";

  public PolicyTemplateEngine() {
    mf = new DefaultMustacheFactory();
  }

  public String createPolicyXml(PdpPolicyDefintion pdpPolicyDefintion) {
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
