package pdp;

import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
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
import pdp.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import pdp.shibboleth.ShibbolethUserDetailService;
import pdp.shibboleth.mock.MockShibbolethFilter;
import pdp.teams.VootClient;
import pdp.web.CsrfProtectionMatcher;
import pdp.web.CsrfTokenResponseHeaderBindingFilter;
import pdp.xacml.PDPEngineHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;

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
  @Autowired
  public PolicyLoader developmentPrePolicyLoader(@Value("${policy.base.dir}") final String policyBaseDir, final PdpPolicyRepository pdpPolicyRepository, final PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    return new DevelopmentPrePolicyLoader(resourceLoader.getResource(policyBaseDir), pdpPolicyRepository, pdpPolicyViolationRepository);
  }

  @Bean
  @Profile({"perf"})
  @Autowired
  public PolicyLoader performancePrePolicyLoader(@Value("${performance.pre.policy.loader.count}") int count, ServiceRegistry serviceRegistry, final PdpPolicyRepository pdpPolicyRepository, final PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    return new PerformancePrePolicyLoader(count, serviceRegistry, pdpPolicyRepository, pdpPolicyViolationRepository);
  }

  @Bean
  @Profile({"test", "acc", "prod"})
  public PolicyLoader noopPolicyLoader() {
    return new NoopPrePolicyLoader();
  }

  @Bean
  @Profile("!prod")
  public ServiceRegistry classPathResourceServiceRegistry(@Value("${spring.profiles.active}") String activeEnvironment) {
    return new ClassPathResourceServiceRegistry(activeEnvironment);
  }

  @Bean
  @Profile("prod")
  public ServiceRegistry urlResourceServiceRegistry(
      @Value("metadata.idpRemotePath") String idpRemotePath,
      @Value("metadata.spRemotePath") String spRemotePath,
      @Value("metadata.userName") String userName,
      @Value("metadata.password") String password,
      @Value("${initial.delay.metadata.refresh.minutes}") int initialDelay,
      @Value("${period.metadata.refresh.minutes}") int period) {
    return new UrlResourceServiceRegistry(idpRemotePath, spRemotePath, userName, password, initialDelay, period);
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
      registry.addInterceptor(new HandlerInterceptorAdapter() {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
          // add this header as an indication to the JS-client that this is a regular, non-session-expired response.
          response.addHeader("X-SESSION-ALIVE", "true");
          return true;
        }
      });
    }
  }

  @Configuration
  @EnableWebSecurity
  public static class ShibbolethSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${policy.enforcement.point.user.name}")
    private String policyEnforcementPointUserName;

    @Value("${policy.enforcement.point.user.password}")
    private String policyEnforcementPointPassword;

    @Value("${policy.idp.access.enforcement}")
    private boolean policyIdpAccessEnforcement;

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private Environment environment;

    @Override
    public void configure(WebSecurity web) throws Exception {
      web
          .ignoring()
          .antMatchers("/health", "/info");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .csrf()
          .requireCsrfProtectionMatcher(new CsrfProtectionMatcher())
          .and()
          .addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(), CsrfFilter.class)
          .addFilterBefore(
              new BasicAuthenticationFilter(getBasicAuthenticationManager()), AbstractPreAuthenticatedProcessingFilter.class
          )
          .authorizeRequests()
          .antMatchers("/decide/**")
          .authenticated()
          .and()
          .addFilterAfter(
              new ShibbolethPreAuthenticatedProcessingFilter(authenticationManagerBean(), serviceRegistry, policyIdpAccessEnforcement),
              BasicAuthenticationFilter.class
          )
          .authorizeRequests()
          .antMatchers("/internal/**")
          .authenticated();

      if (environment.acceptsProfiles("no-csrf")) {
        http.csrf().disable();
      }
      if (environment.acceptsProfiles("dev", "perf", "no-csrf")) {
        //we can't use @Profile, because we need to add it before the real filter
        http.addFilterBefore(new MockShibbolethFilter(), ShibbolethPreAuthenticatedProcessingFilter.class);
      }
    }

    private AuthenticationManager getBasicAuthenticationManager() {
      return authentication -> {
        if (authentication.getPrincipal().equals(policyEnforcementPointUserName)
            && authentication.getCredentials().equals(policyEnforcementPointPassword)) {
          return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), Arrays.asList(new SimpleGrantedAuthority("PEP")));
        }
        return null;
      };
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
      authenticationProvider.setPreAuthenticatedUserDetailsService(new ShibbolethUserDetailService());
      auth.authenticationProvider(authenticationProvider);
    }
  }

  public static <T> Collector<T, List<T>, Optional<T>> singletonOptionalCollector() {
    return Collector.of(ArrayList::new, List::add, (left, right) -> {
          left.addAll(right);
          return left;
        }, list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0))
    );
  }

  public static <T> Collector<T, List<T>, T> singletonCollector() {
    return Collector.of(ArrayList::new, List::add, (left, right) -> {
          left.addAll(right);
          return left;
        }, list -> {
          if (list.isEmpty()) {
            throw new RuntimeException("Expected at least one element in the List");
          }
          return list.get(0);
    }
    );
  }
}