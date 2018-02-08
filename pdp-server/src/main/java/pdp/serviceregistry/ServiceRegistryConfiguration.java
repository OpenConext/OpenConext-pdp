package pdp.serviceregistry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.MalformedURLException;

@Configuration
public class ServiceRegistryConfiguration {

    @Bean
    @Profile({"dev", "no-csrf", "perf", "mail"})
    public ServiceRegistry classPathResourceServiceRegistry() {
        return new ClassPathResourceServiceRegistry(true);
    }


    @Bean
    @Profile({"test", "acc", "prod"})
    public ServiceRegistry urlResourceServiceRegistry(
        @Value("${metadata.username}") String username,
        @Value("${metadata.password}") String password,
        @Value("${metadata.manageBaseUrl}") String manageBaseUrl,
        @Value("${period.metadata.refresh.minutes}") int period) {
        return new UrlResourceServiceRegistry(username, password, manageBaseUrl, period);
    }


}
