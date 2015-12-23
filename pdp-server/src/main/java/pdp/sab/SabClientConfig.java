package pdp.sab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;

@Configuration
public class SabClientConfig {

  @Bean
  @Profile({"test", "acc", "prod"})
  public SabClient sabClient(@Value("${sab.userName}") String userName,
                             @Value("${sab.password}") String password,
                             @Value("${sab.endpoint}") String endpoint) {
    return new SabClient(userName, password, endpoint);
  }

  @Bean
  @Profile({"dev", "perf", "no-csrf"})
  public SabClient mockSabClient(@Value("${sab.userName}") String userName,
                                 @Value("${sab.password}") String password,
                                 @Value("${sab.endpoint}") String endpoint) {
    return new SabClient(userName, password, endpoint) {
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
