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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import pdp.xacml.*;

import java.io.IOException;

@SpringBootApplication()
public class PdpApplication {

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private PdpPolicyRepository pdpPolicyRepository;

  public static void main(String[] args) {
    SpringApplication.run(PdpApplication.class, args);
  }

  @Bean
  @Autowired
  public PDPEngine pdpEngine(
      @Value("${xacml.properties.path}") final String xacmlPropertiesFileLocation) throws IOException, FactoryException {
    Resource resource = resourceLoader.getResource(xacmlPropertiesFileLocation);
    String absolutePath = resource.getFile().getAbsolutePath();

    //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
    System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

    PDPEngineFactory factory = PDPEngineFactory.newInstance();
    //We want to be properties driven for testability, but we can't otherwise hook into the PdpPolicyRepository
    if (factory instanceof OpenConextPDPEngineFactory) {
      return ((OpenConextPDPEngineFactory) factory).newEngine(pdpPolicyRepository);
    } else {
      return factory.newEngine();
    }

  }

  @Configuration
  public static class WebMvcConfig extends WebMvcConfigurerAdapter {
  }

  @Configuration
  public static class RestMvcConfiguration extends RepositoryRestMvcConfiguration {

    @Override
    public RepositoryRestConfiguration config() {
      RepositoryRestConfiguration config = super.config();
      config.setBaseUri("/api");
      config.setDefaultMediaType(MediaType.APPLICATION_JSON);
      config.setReturnBodyOnCreate(true);
      return config;
    }
  }

}