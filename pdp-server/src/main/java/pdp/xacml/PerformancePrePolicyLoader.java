package pdp.xacml;

import org.springframework.core.io.ByteArrayResource;
import pdp.PolicyTemplateEngine;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.repositories.PdpPolicyRepository;
import pdp.serviceregistry.ServiceRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class PerformancePrePolicyLoader extends DevelopmentPrePolicyLoader {

  private final ServiceRegistry serviceRegistry;
  private final PolicyTemplateEngine templateEngine = new PolicyTemplateEngine();
  private Random random = new Random();

  public PerformancePrePolicyLoader(ServiceRegistry serviceRegistry, PdpPolicyRepository pdpPolicyRepository) {
    super(new ByteArrayResource("noop".getBytes()), pdpPolicyRepository);
    this.serviceRegistry = serviceRegistry;
  }

  @Override
  public List<PdpPolicy> getPolicies() {
    // for every ServiceProvider create a policy
    String uuid = UUID.randomUUID().toString();
    return serviceRegistry.serviceProviders().stream().map(sp -> pdpPolicyDefinition(sp, uuid))
        .map(def -> new PdpPolicy(templateEngine.createPolicyXml(def), "Performance_Policy_" + uuid))
        .collect(toList());
  }

  private PdpPolicyDefinition pdpPolicyDefinition(EntityMetaData sp, String uuid) {
    //TODO random values
    PdpPolicyDefinition definition = new PdpPolicyDefinition();
    definition.setAllAttributesMustMatch(true);
    definition.setDenyAdvice("Not authorized");
    definition.setDenyAdviceNl("Niet geautoriseerd");
    definition.setDenyRule(true);
    definition.setDescription("Performance Policy "+ uuid);
    String entityId = serviceRegistry.identityProviders().get(random.nextInt(serviceRegistry.identityProviders().size())).getEntityId();
    definition.setIdentityProviderIds(Arrays.asList(entityId));
    return definition;
  }

}
