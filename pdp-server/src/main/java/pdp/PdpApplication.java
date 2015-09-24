package pdp;

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
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import pdp.repositories.PdpPolicyRepository;
import pdp.xacml.PDPEngineHolder;
import pdp.teams.VootClient;

import java.io.IOException;
import java.util.Locale;

@SpringBootApplication()
public class PdpApplication {

  @Autowired
  private ResourceLoader resourceLoader;

  public static void main(String[] args) {
    SpringApplication.run(PdpApplication.class, args);
  }

  @Bean
  @Autowired
  public PDPEngineHolder pdpEngine(
      @Value("${xacml.properties.path}") final String xacmlPropertiesFileLocation,
      final PdpPolicyRepository pdpPolicyRepository, final VootClient vootClient
  ) throws IOException, FactoryException {
    Resource resource = resourceLoader.getResource(xacmlPropertiesFileLocation);
    String absolutePath = resource.getFile().getAbsolutePath();

    //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
    System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

    return new PDPEngineHolder(pdpPolicyRepository, vootClient);
  }

  @Configuration
  public static class WebMvcConfig extends WebMvcConfigurerAdapter {
  }

  @Configuration
  public static class RestMvcConfiguration extends RepositoryRestMvcConfiguration {

    @Override
    public RepositoryRestConfiguration config() {
      RepositoryRestConfiguration config = super.config();
      config.setDefaultMediaType(MediaType.APPLICATION_JSON);
      config.setReturnBodyOnCreate(true);
      return config;
    }
  }

}