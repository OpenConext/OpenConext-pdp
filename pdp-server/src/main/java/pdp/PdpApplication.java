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
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import pdp.domain.PdpPolicyViolation;
import pdp.repositories.PdpPolicyRepository;
import pdp.serviceregistry.ClassPathResourceServiceRegistry;
import pdp.serviceregistry.ServiceRegistry;
import pdp.serviceregistry.UrlResourceServiceRegistry;
import pdp.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import pdp.shibboleth.ShibbolethUserDetailService;
import pdp.shibboleth.mock.MockShibbolethFilter;
import pdp.teams.VootClient;
import pdp.xacml.DevelopmentPrePolicyLoader;
import pdp.xacml.PDPEngineHolder;

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
      final Environment environment,
      final PdpPolicyRepository pdpPolicyRepository, final VootClient vootClient
  ) throws IOException, FactoryException {
    Resource resource = resourceLoader.getResource(xacmlPropertiesFileLocation);
    String absolutePath = resource.getFile().getAbsolutePath();

    //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
    System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

    if (environment.acceptsProfiles("dev")) {
      new DevelopmentPrePolicyLoader().loadPolicies(pdpPolicyRepository);
    }

    return new PDPEngineHolder(pdpPolicyRepository, vootClient);
  }

  @Bean
  @Profile("!production")
  public ServiceRegistry classPathResourceServiceRegistry() {
    return new ClassPathResourceServiceRegistry();
  }

  @Bean
  @Profile("production")
  public ServiceRegistry urlResourceServiceRegistry(@Value("${initial.delay.metadata.refresh.minutes}") int initialDelay,
                                                    @Value("${period.metadata.refresh.minutes}") int period) {
    return new UrlResourceServiceRegistry(initialDelay, period);
  }

  @Configuration
  @EnableWebSecurity
  public static class ShibbolethSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${policy.enforcement.point.user.name}")
    private String policyEnforcementPointUserName;

    @Value("${policy.enforcement.point.user.password}")
    private String policyEnforcementPointPassword;

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Bean
    @Profile("dev")
    public FilterRegistrationBean mockShibbolethFilter() {
      FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
      filterRegistrationBean.setFilter(new MockShibbolethFilter());
      filterRegistrationBean.addUrlPatterns("/*");
      return filterRegistrationBean;
    }

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
          .disable()
          .addFilterBefore(
              new BasicAuthenticationFilter(getBasicAuthenticationManager()), AbstractPreAuthenticatedProcessingFilter.class
          )
          .authorizeRequests()
          .antMatchers("/decide/**")
          .authenticated()
          .and()
          .addFilterAfter(
              new ShibbolethPreAuthenticatedProcessingFilter(authenticationManagerBean(), serviceRegistry),
              BasicAuthenticationFilter.class
          )
          .authorizeRequests()
          .antMatchers("/internal/**")
          .authenticated()
          .and()
          .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
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
        }, list -> {
          if (list.isEmpty()) {
            return Optional.empty();
          }
          return Optional.of(list.get(0));
        }
    );
  }

}