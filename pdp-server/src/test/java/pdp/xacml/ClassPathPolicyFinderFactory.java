package pdp.xacml;

import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.pdp.policy.Policy;
import org.apache.openaz.xacml.pdp.policy.PolicyDef;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.std.StdPolicyFinderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import pdp.PolicyTemplateEngine;
import pdp.domain.PdpPolicyDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;

/**
 * StdPolicyFinderFactory works with absolute file names and we want to work with classpath resources
 */
public class ClassPathPolicyFinderFactory extends StdPolicyFinderFactory {

  public static final String POLICY_FILES = "policy.files.key";

  public static final String PARSE_POLICY_XML = "parse.policy.xml";

  private PdpPolicyDefinitionParser policyDefinitionParser = new PdpPolicyDefinitionParser();

  private PolicyTemplateEngine policyTemplateEngine = new PolicyTemplateEngine();

  @Override
  protected List<PolicyDef> getPolicyDefs(String propertyName, Properties properties) {
    String policyFiles = System.getProperty(POLICY_FILES);
    Assert.notNull(policyFiles, "One ore more comma seperated policy file locations are requried in the " + POLICY_FILES + " system properties");
    return Arrays.asList(policyFiles.split(",")).stream().map(policyFile -> loadPolicyDef(policyFile)).collect(toList());
  }

  private PolicyDef loadPolicyDef(String policyFile) {
    try {
      String policyXml = IOUtils.toString(getPolicyInputStream(policyFile));
      Policy policyDef = PdpPolicyDefinitionParser.parsePolicy(policyXml);
      return policyDef;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private InputStream getPolicyInputStream(String policyFile) throws IOException {
    boolean parsePolicyXml = Boolean.parseBoolean(System.getProperty(PARSE_POLICY_XML));
    ClassPathResource resource = new ClassPathResource("xacml/test-policies/" + policyFile);
    if (parsePolicyXml) {
      PdpPolicyDefinition policyDefinition = policyDefinitionParser.parse(1L, policyFile, IOUtils.toString(resource.getInputStream()));
      String policyXml = policyTemplateEngine.createPolicyXml(policyDefinition);
      return IOUtils.toInputStream(policyXml);
    } else {
      return resource.getInputStream();
    }
  }

}
