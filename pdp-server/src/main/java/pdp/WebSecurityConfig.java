package pdp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;
import pdp.access.BasicAuthenticationManager;
import pdp.access.PolicyIdpAccessEnforcerFilter;
import pdp.serviceregistry.ServiceRegistry;
import pdp.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import pdp.shibboleth.ShibbolethUserDetailService;
import pdp.shibboleth.mock.MockShibbolethFilter;
import pdp.web.CsrfProtectionMatcher;
import pdp.web.CsrfTokenResponseHeaderBindingFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  public static final String NON_SHIBBOLETH_PROTECTED_METHODS = "^(?!/protected/*|/decide/policy|/health|/info).*$";

  @Value("${policy.enforcement.point.user.name}")
  private String policyEnforcementPointUserName;

  @Value("${policy.enforcement.point.user.password}")
  private String policyEnforcementPointPassword;

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
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and()
        .csrf()
        .requireCsrfProtectionMatcher(new CsrfProtectionMatcher()).and()
        .addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(), CsrfFilter.class)
        .addFilterBefore(
            new PolicyIdpAccessEnforcerFilter(new BasicAuthenticationManager(policyEnforcementPointUserName, policyEnforcementPointPassword), serviceRegistry),
            AbstractPreAuthenticatedProcessingFilter.class
        )
        .addFilterAfter(new RegExpRequestMatcherFilter(
            new ShibbolethPreAuthenticatedProcessingFilter(authenticationManagerBean(), serviceRegistry),
            NON_SHIBBOLETH_PROTECTED_METHODS),
            PolicyIdpAccessEnforcerFilter.class
        )
        .authorizeRequests()
        .antMatchers("/protected/**").hasAnyRole("PEP", "ADMIN")
        .antMatchers("/decide/**").hasAnyRole("PEP", "ADMIN")
        .antMatchers("/internal/**").hasAnyRole("PEP", "ADMIN")
        .antMatchers("/public/**", "/health/**", "/info/**").permitAll()
        .antMatchers("/**").hasRole("USER");

    if (environment.acceptsProfiles("no-csrf")) {
      http.csrf().disable();
    }

    if (environment.acceptsProfiles("dev", "perf")) {
      //we can't use @Profile, because we need to add it before the real filter
      http.addFilterBefore(new MockShibbolethFilter(), ShibbolethPreAuthenticatedProcessingFilter.class);
    }
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
    authenticationProvider.setPreAuthenticatedUserDetailsService(new ShibbolethUserDetailService());
    auth.authenticationProvider(authenticationProvider);
  }
}