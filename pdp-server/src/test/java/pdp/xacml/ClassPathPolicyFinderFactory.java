package pdp.xacml;

import org.apache.openaz.xacml.pdp.policy.Policy;
import org.apache.openaz.xacml.pdp.policy.PolicyDef;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.std.StdPolicyFinderFactory;
import org.apache.openaz.xacml.std.StdStatusCode;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * StdPolicyFinderFactory works with absolute file names and we want to work with classpath resources
 */
public class ClassPathPolicyFinderFactory extends StdPolicyFinderFactory {

  private static Logger LOG = LoggerFactory.getLogger(ClassPathPolicyFinderFactory.class);

  public static String POLICY_FILES = "policy.files.key";

  @Override
  protected List<PolicyDef> getPolicyDefs(String propertyName, Properties properties) {
    String policyFiles = System.getProperty(POLICY_FILES);
    Assert.notNull(policyFiles, "One ore more comma seperated policy file locations are requried in the " +POLICY_FILES+ " system properties");
    return Arrays.asList(policyFiles.split(",")).stream().map(policyFile -> loadPolicyDef(policyFile)).collect(toList());
  }

  private PolicyDef loadPolicyDef(String policyFile) {
    ClassPathResource resource = new ClassPathResource("xacml/test-policies/"+policyFile);
    try {
      LOG.info("Loading policy file " + getAbsolutePath(resource));
      return DOMPolicyDef.load(resource.getInputStream());
    } catch (DOMStructureException e) {
      LOG.error("Error loading policy file " + getAbsolutePath(resource), e);
      return new Policy(StdStatusCode.STATUS_CODE_SYNTAX_ERROR, e.getMessage());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getAbsolutePath(ClassPathResource resource) {
    try {
      return resource.getFile().getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
