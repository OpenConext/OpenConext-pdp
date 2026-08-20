package pdp;

import lombok.SneakyThrows;
import org.apache.hc.core5.http.HttpHost;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import tools.jackson.databind.ObjectMapper;
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
import pdp.web.HttpHostProvider;
import pdp.xacml.PDPEngineHolder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@SpringBootApplication(exclude = {
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

    @SneakyThrows
    @Bean
    public WebClient webClient(ClientRegistrationRepository clients,
                               OAuth2AuthorizedClientRepository authClients,
                               @Value("${voot.serviceUrl}") String vootServiceUrl) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(clients, authClients);
        oauth2.setDefaultClientRegistrationId("voot");

        HttpClient httpClient = HttpClient.create();
        Optional<HttpHost> proxyHost = HttpHostProvider.resolveHttpHost(URI.create(vootServiceUrl).toURL());
        if (proxyHost.isPresent()) {
            HttpHost httpHost = proxyHost.get();
            httpClient = httpClient.proxy(proxySpec -> proxySpec.type(ProxyProvider.Proxy.HTTP)
                .host(httpHost.getHostName())
                .port(httpHost.getPort()));
        }

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .apply(oauth2.oauth2Configuration())
            .build();
    }

}
