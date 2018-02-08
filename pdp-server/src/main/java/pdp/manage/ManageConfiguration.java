package pdp.manage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ManageConfiguration {

    @Bean
    @Profile({"dev", "no-csrf", "perf", "mail"})
    public Manage classPathResourceServiceRegistry() {
        return new ClassPathResourceManage(true);
    }


    @Bean
    @Profile({"test", "acc", "prod"})
    public Manage urlResourceServiceRegistry(
        @Value("${metadata.username}") String username,
        @Value("${metadata.password}") String password,
        @Value("${metadata.manageBaseUrl}") String manageBaseUrl,
        @Value("${period.metadata.refresh.minutes}") int period) {
        return new UrlResourceManage(username, password, manageBaseUrl, period);
    }


}
