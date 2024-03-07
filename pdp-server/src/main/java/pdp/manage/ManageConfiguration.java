package pdp.manage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ManageConfiguration {

//    @Bean
//    @Profile({"dev", "local", "no-csrf", "perf", "mail"})
//    public Manage classPathResourceManage() {
//        return new ClassPathResourceManage();
//    }


    @Bean
//    @Profile({"test", "acc", "prod"})
    public Manage urlResourceManage(
        @Value("${manage.username}") String username,
        @Value("${manage.password}") String password,
        @Value("${manage.manageBaseUrl}") String manageBaseUrl) {
        return new UrlResourceManage(username, password, manageBaseUrl);
    }


}
