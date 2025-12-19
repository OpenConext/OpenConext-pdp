package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import pdp.repositories.PdpPolicyRepository;
import pdp.sab.SabClient;
import pdp.stats.StatsContextHolder;
import pdp.teams.VootClient;
import pdp.xacml.PDPEngineHolder;

import java.io.IOException;

@SpringBootApplication(exclude = {
    FreeMarkerAutoConfiguration.class,
    AuditAutoConfiguration.class,
    MetricsAutoConfiguration.class})
public class PdpApplication {

    @Autowired
    private ResourceLoader resourceLoader;

    public static void main(String[] args) {
        SpringApplication.run(PdpApplication.class, args);
    }

    @Bean
    public StatsContextHolder statsContextHolder(ObjectMapper objectMapper) {
        return new StatsContextHolder("decide/policy", objectMapper);
    }

    @Bean
    public PDPEngineHolder pdpEngine(
        @Value("${xacml.properties.path}") final String xacmlPropertiesFileLocation,
        final PdpPolicyRepository pdpPolicyRepository,
        final VootClient vootClient,
        final SabClient sabClient
    ) throws IOException, FactoryException {
        Resource resource = resourceLoader.getResource(xacmlPropertiesFileLocation);
        String absolutePath = resource.getFile().getAbsolutePath();

        //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
        System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

        return new PDPEngineHolder(pdpPolicyRepository, vootClient, sabClient);
    }

    @Bean
    public WebClient webClient(ClientRegistrationRepository clients,
                               OAuth2AuthorizedClientRepository authClients) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);
        oauth2.setDefaultClientRegistrationId("voot");

        return WebClient.builder()
            .apply(oauth2.oauth2Configuration())
            .build();
    }

}
