package pdp.sab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import pdp.teams.VootClient;
import pdp.teams.VootClientConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;

@Configuration
public class SabClientConfig {

  @Value("${sab.userName}")
  private String userName;

  @Value("${sab.password}")
  private String password;

  @Value("${sab.endpoint}")
  private String endpoint;

  @Bean
  @Profile({"test", "acc", "prod"})
  public SabClient sabClient() {
    return new SabClient(userName,password,endpoint);
  }

  @Bean
  @Profile({"dev", "perf", "no-csrf"})
  public SabClient mockSabClient() {
    return new SabClient(userName,password,endpoint) {
      @Override
      public List<String> roles(String userUrn) {
        /*
         * These are the groups names defined in the test set of policies
         */
        return URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN.equals(userUrn) ?
            Arrays.asList(
                "OperationeelBeheerder",
                "Instellingsbevoegde"
            ) : Collections.EMPTY_LIST;
      }
    };
  }

}
