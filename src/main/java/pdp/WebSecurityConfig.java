package pdp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import pdp.access.BasicAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Value("${policy.enforcement.point.user.name}")
    private String policyEnforcementPointUserName;

    @Value("${policy.enforcement.point.user.password}")
    private String policyEnforcementPointPassword;


    @Bean
    public SecurityFilterChain apiSecurityConfigurationChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/internal/health", "/internal/info", "/public/**").permitAll()
                        .requestMatchers("/pdp/api/protected/**", "/pdp/api/decide/policy", "/pdp/api/manage/**").hasRole("PEP")
                        .requestMatchers("/pdp/api/internal/**").authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(
                        new BasicAuthenticationFilter(
                                new BasicAuthenticationProvider(policyEnforcementPointUserName, policyEnforcementPointPassword)
                        ), BasicAuthenticationFilter.class
                )
                .build();
    }

}
