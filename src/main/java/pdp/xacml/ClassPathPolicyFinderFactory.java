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
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Properties;

/**
 * StdPolicyFinderFactory works with absolute file names and we want to work with classpath resources
 */
public class ClassPathPolicyFinderFactory extends StdPolicyFinderFactory {

  private static Logger LOG = LoggerFactory.getLogger(ClassPathPolicyFinderFactory.class);

  private DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

  @Override
  protected PolicyDef loadPolicyDef(String policyId, Properties properties) {
    String propLocation = properties.getProperty(policyId + PROP_FILE);
    Assert.notNull(propLocation, policyId + PROP_FILE + " is null");
    Resource resource = resourceLoader.getResource(propLocation);
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

  private String getAbsolutePath(Resource resource) {
    try {
      return resource.getFile().getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
