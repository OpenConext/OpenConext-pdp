package pdp;

import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import pdp.policies.PolicyLoader;
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
    public StatsContextHolder statsContextHolder() {
        return new StatsContextHolder("decide/policy");
    }

    @Bean
    public PDPEngineHolder pdpEngine(
            @Value("${xacml.properties.path}") final String xacmlPropertiesFileLocation,
            final PdpPolicyRepository pdpPolicyRepository,
            final VootClient vootClient,
            final SabClient sabClient,
            final PolicyLoader policyLoader
    ) throws IOException, FactoryException {
        Resource resource = resourceLoader.getResource(xacmlPropertiesFileLocation);
        String absolutePath = resource.getFile().getAbsolutePath();

        //This will be picked up by the XACML bootstrapping when creating a new PDPEngine
        System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, absolutePath);

        policyLoader.loadPolicies();

        return new PDPEngineHolder(pdpPolicyRepository, vootClient, sabClient);
    }

}