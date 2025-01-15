package pdp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import pdp.access.BasicAuthenticationProvider;
import pdp.access.PolicyIdpAccessEnforcerFilter;
import pdp.manage.Manage;
import pdp.web.CsrfProtectionMatcher;
import pdp.web.CsrfTokenResponseHeaderBindingFilter;

@Configuration
@EnableWebSecurity
@Order(1)
public class WebSecurityConfig {

    @Value("${policy.enforcement.point.user.name}")
    private String policyEnforcementPointUserName;

    @Value("${policy.enforcement.point.user.password}")
    private String policyEnforcementPointPassword;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        BasicAuthenticationProvider basicAuthenticationProvider =
                new BasicAuthenticationProvider(policyEnforcementPointUserName, policyEnforcementPointPassword);
        auth.authenticationProvider(basicAuthenticationProvider);

    }

    @Configuration
    @Order(2)
    public static class ApiSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private Manage manage;

        @Override
        public void configure(WebSecurity web) throws Exception {
            web
                    .ignoring()
                    .antMatchers("/internal/health", "/internal/info", "/public/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/**")
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .csrf()
                    .disable()
                    .addFilterBefore(
                            new PolicyIdpAccessEnforcerFilter(authenticationManager(), manage),
                            BasicAuthenticationFilter.class
                    )
                    .authorizeRequests()
                    .antMatchers("/protected/**", "/decide/policy", "/manage/**")
                    .hasAnyRole("PEP", "ADMIN");
        }

    }
}