package pdp;

import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceWebFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import pdp.policies.DevelopmentPrePolicyLoader;
import pdp.policies.NoopPrePolicyLoader;
import pdp.policies.PerformancePrePolicyLoader;
import pdp.policies.PolicyLoader;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.sab.SabClient;
import pdp.serviceregistry.ClassPathResourceServiceRegistry;
import pdp.serviceregistry.ServiceRegistry;
import pdp.serviceregistry.UrlResourceServiceRegistry;
import pdp.teams.VootClient;
import pdp.web.SessionAliveInterceptor;
import pdp.xacml.PDPEngineHolder;

import java.io.IOException;
import java.net.MalformedURLException;

@SpringBootApplication(exclude = {ManagementWebSecurityAutoConfiguration.class, SecurityAutoConfiguration.class, TraceWebFilterAutoConfiguration.class, MetricFilterAutoConfiguration.class})
public class PdpApplication {

  @Autowired
  private ResourceLoader resourceLoader;

  public static void main(String[] args) {
    SpringApplication.run(PdpApplication.class, args);
  }

  @Bean
  public PDPEngineHolder pdpEngine(
      @Value("${xacml.properties.path}") final String xacmlPropertiesFileLocation,
      final PdpPolicyRepository pdpPolicyRepository,
      final VootClient vootClient,
      final SabClient sabClient,
      final PolicyLoader policyLoader
  ) throws IOException, FactoryException {
    Resource resource = resourceLoader.getResource(xacmlPropertiesFileLocation);
    String absolutePath = resource.getFile().getAbsolutePath();

    //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
    System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

    policyLoader.loadPolicies();

    return new PDPEngineHolder(pdpPolicyRepository, vootClient, sabClient);
  }

  @Bean
  @Profile({"dev", "no-csrf"})
  public PolicyLoader developmentPrePolicyLoader(@Value("${policy.base.dir}") String policyBaseDir, PdpPolicyRepository pdpPolicyRepository, PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    return new DevelopmentPrePolicyLoader(resourceLoader.getResource(policyBaseDir), pdpPolicyRepository, pdpPolicyViolationRepository);
  }

  @Bean
  @Profile({"perf"})
  public PolicyLoader performancePrePolicyLoader(@Value("${performance.pre.policy.loader.count}") int count, ServiceRegistry serviceRegistry, PdpPolicyRepository pdpPolicyRepository, PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    return new PerformancePrePolicyLoader(count, serviceRegistry, pdpPolicyRepository, pdpPolicyViolationRepository);
  }

  @Bean
  @Profile({"test", "acc", "prod"})
  public PolicyLoader noopPolicyLoader() {
    return new NoopPrePolicyLoader();
  }

  @Bean
  @Profile({"dev", "no-csrf"})
  public ServiceRegistry classPathResourceServiceRegistry() {
    return new ClassPathResourceServiceRegistry(true);
  }


  @Bean
  @Profile({"test","acc","prod"})
  public ServiceRegistry urlResourceServiceRegistry(
      @Value("${metadata.username}") String username,
      @Value("${metadata.password}") String password,
      @Value("${metadata.idpRemotePath}") String idpRemotePath,
      @Value("${metadata.spRemotePath}") String spRemotePath,
      @Value("${period.metadata.refresh.minutes}") int period) throws MalformedURLException {
    return new UrlResourceServiceRegistry(username, password, idpRemotePath, spRemotePath, period);
  }

  @Bean
  public PolicyViolationRetentionPeriodCleaner policyViolationRetentionPeriodCleaner(@Value("${policy.violation.retention.period.days}") int retentionPeriodDays,
                                                                                     PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    return new PolicyViolationRetentionPeriodCleaner(retentionPeriodDays, pdpPolicyViolationRepository);
  }

  @Configuration
  public static class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
      super.addInterceptors(registry);
      registry.addInterceptor(new SessionAliveInterceptor());
    }
  }

}