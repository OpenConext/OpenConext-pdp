package pdp.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class MailConfiguration {

  @Value("${email.base_url}")
  private String baseUrl;

  @Value("${email.from}")
  private String emailFrom;

  @Value("${email.to}")
  private String emailTo;

  @Bean
  @Profile({"test", "acc", "prod", "mail"})
  public MailBox mailSenderProd() {
    return new DefaultMailBox(baseUrl, emailTo, emailFrom );
  }

  @Bean
  @Profile({"dev", "no-csrf", "perf"})
  @Primary
  public MailBox mailSenderDev() {
    return new MockMailBox(baseUrl, emailTo, emailFrom);
  }
}
