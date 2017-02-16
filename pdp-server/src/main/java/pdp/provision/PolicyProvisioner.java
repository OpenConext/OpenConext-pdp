package pdp.provision;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import pdp.JsonMapper;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.repositories.PdpPolicyRepository;
import pdp.xacml.PolicyTemplateEngine;

import java.io.IOException;
import java.util.stream.Stream;

@Service
public class PolicyProvisioner implements ApplicationListener<ContextStartedEvent>, JsonMapper {

    private final PolicyTemplateEngine policyTemplateEngine = new PolicyTemplateEngine();

    private final PdpPolicyRepository pdpPolicyRepository;

    private final String path;

    @Autowired
    public PolicyProvisioner(@Value("${provision.policies.directory}") String path, PdpPolicyRepository pdpPolicyRepository) {
        this.path = path;
        this.pdpPolicyRepository = pdpPolicyRepository;
    }


    @Override
    public void onApplicationEvent(ContextStartedEvent contextRefreshedEvent) {
        policyDefinitions()
            .filter(definition -> pdpPolicyRepository.findByNameAndLatestRevision(definition.getName(), true).isPresent())
            .forEach(definition -> {
                String policyXml = policyTemplateEngine.createPolicyXml(definition);
                PdpPolicy pdpPolicy = new PdpPolicy(policyXml, definition.getName(), true, "provisioned",
                    "surfnet", "system", true);
                pdpPolicyRepository.save(pdpPolicy);
            });
    }

    private PdpPolicyDefinition parse(Resource resource) {
        try {
            return objectMapper.readValue(resource.getInputStream(), PdpPolicyDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<PdpPolicyDefinition> policyDefinitions() {
        try {
            return Stream.of(new PathMatchingResourcePatternResolver()
                .getResources(String.format("classpath:%s/*.json", path)))
                .map(this::parse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
