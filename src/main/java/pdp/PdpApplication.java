package pdp;

import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPEngineFactory;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@SpringBootApplication()
public class PdpApplication {
  public static void main(String[] args) {
    SpringApplication.run(PdpApplication.class, args);
  }

  @Autowired
  private ResourceLoader resourceLoader;

  @Bean
  @Autowired
  public PDPEngine pdpEngine(
      @Value("${xacml.properties.path}") final String xacmlPropertiesFileLocation) throws IOException, FactoryException {
    ClassPathResource xacmlResource = new ClassPathResource(xacmlPropertiesFileLocation);
    String absolutePath = xacmlResource.getFile().getAbsolutePath();
    System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);
    PDPEngineFactory factory = PDPEngineFactory.newInstance();
    PDPEngine pdpEngine = factory.newEngine();
    return pdpEngine;
  }
}