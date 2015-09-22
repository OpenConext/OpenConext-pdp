package pdp.xacml.teams;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class VootClientConfig {

  @Value("${voot.accessTokenUri}")
  private String accessTokenUri;

  @Value("${voot.userAuthorizationUri}")
  private String userAuthorizationUri;

  @Value("${voot.clientId}")
  private String clientId;

  @Value("${voot.clientSecret}")
  private String clientSecret;

  @Value("${voot.scopes}")
  private String spaceDelimitedScopes;

  @Value("${voot.serviceUrl}")
  private String vootServiceUrl;

  @Bean
  @Profile("!dev")
  public VootClient vootClient() {
    return new VootClient(vootRestTemplate(), vootServiceUrl);
  }

  @Bean
  @Profile("dev")
  public VootClient mockVootClient() {
    return new VootClient(vootRestTemplate(), vootServiceUrl) {
      @Override
      public List<String> groups(String userUrn) {
        return "urn:collab:person:example.com:admin".equals(userUrn) ? Arrays.asList("urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo") : Collections.EMPTY_LIST;
      }
    };
  }

  private OAuth2RestTemplate vootRestTemplate() {
    ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
    details.setId("pdp");
    details.setClientId(clientId);
    details.setClientSecret(clientSecret);
    details.setAccessTokenUri(accessTokenUri);
    details.setScope(Arrays.asList(spaceDelimitedScopes.split(" ")));
    return new OAuth2RestTemplate(details);
  }

}
