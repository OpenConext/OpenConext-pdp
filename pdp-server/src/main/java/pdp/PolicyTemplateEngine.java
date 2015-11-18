package pdp;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.domain.PdpPolicyDefinition;

import java.io.IOException;
import java.io.StringWriter;

public class PolicyTemplateEngine {

  private MustacheFactory mf = new DefaultMustacheFactory();

  private final static Logger LOG = LoggerFactory.getLogger(PolicyTemplateEngine.class);

  public String createPolicyXml(PdpPolicyDefinition pdpPolicyDefintion) {
    Mustache mustache = mf.compile("templates/policy-definition.xml");
    StringWriter writer = new StringWriter();
    try {
      mustache.execute(writer, pdpPolicyDefintion).flush();
      String policyXml = writer.toString();
      LOG.debug("Returning policyXml {}", policyXml);
      return policyXml;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getPolicyId(String name) {
    if (name == null) {
      return null;
    }
    return "urn:surfconext:xacml:policy:id:" + name.replaceAll("[^\\w]+", "_").toLowerCase();
  }

}
