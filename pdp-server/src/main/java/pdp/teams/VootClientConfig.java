package pdp.teams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

@Configuration
public class VootClientConfig {

    public static final String URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN = "urn:collab:person:example.com:admin";

    @Value("${voot.accessTokenUri}")
    private String accessTokenUri;

    @Value("${voot.clientId}")
    private String clientId;

    @Value("${voot.clientSecret}")
    private String clientSecret;

    @Value("${voot.scopes}")
    private String spaceDelimitedScopes;

    @Value("${voot.serviceUrl}")
    private String vootServiceUrl;

    @Bean
    @Profile({"devconf", "test", "acc", "prod"})
    public VootClient vootClient() {
        return new VootClient(vootRestTemplate(), vootServiceUrl);
    }

    @Bean
    @Profile({"dev", "perf", "no-csrf", "mail", "local"})
    public VootClient mockVootClient() {
        return new VootClient(vootRestTemplate(), vootServiceUrl) {
            @Override
            public List<String> groups(String userUrn) {
                /*
                 * These are the groups names defined in the test set of policies
                 */
                return URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN.equals(userUrn) ?
                        asList(
                                "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo",
                                "urn:collab:group:avans.nl:HRemployees",
                                "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo",
                                "urn:collab:group:surfteams.nl:nl:surfnet:diensten:SURFnetWikiAccess"
                        ) : Collections.emptyList();
            }
        };
    }

    @SuppressWarnings("deprecation")
    private OAuth2RestTemplate vootRestTemplate() {
        ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
        details.setId("pdp");
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setAccessTokenUri(accessTokenUri);
        details.setScope((spaceDelimitedScopes != null) ? asList(spaceDelimitedScopes.split(" ")) : Collections.emptyList());
        return new OAuth2RestTemplate(details);
    }

}
