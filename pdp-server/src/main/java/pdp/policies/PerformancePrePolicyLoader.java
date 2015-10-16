package pdp.policies;

import org.springframework.core.io.ByteArrayResource;
import pdp.PolicyTemplateEngine;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpAttribute;
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
    return serviceRegistry.serviceProviders().stream().map(sp -> pdpPolicyDefinition(sp, UUID.randomUUID().toString()))
        .map(def -> new PdpPolicy(templateEngine.createPolicyXml(def), def.getName()))
        .collect(toList());
  }

  private PdpPolicyDefinition pdpPolicyDefinition(EntityMetaData sp, String uuid) {
    PdpPolicyDefinition definition = new PdpPolicyDefinition();
    definition.setName("Performance_Policy_" + uuid);
    definition.setDescription("Performance Policy " + uuid);
    definition.setServiceProviderId(sp.getEntityId());
    definition.setDenyAdvice("Not authorized");
    definition.setDenyAdviceNl("Niet geautoriseerd");
    definition.setDescription("Performance Policy " + uuid);
    EntityMetaData idp = serviceRegistry.identityProviders().get(random.nextInt(serviceRegistry.identityProviders().size()));
    definition.setIdentityProviderIds(Arrays.asList(idp.getEntityId()));
    List<PdpAttribute> attributes = Arrays.asList(
        new PdpAttribute("urn:mace:dir:attribute-def:eduPersonAffiliation", "teacher"),
        new PdpAttribute("urn:mace:dir:attribute-def:eduPersonAffiliation", "staff"),
        new PdpAttribute("urn:mace:dir:attribute-def:eduPersonEntitlement", "urn:mace:example.org:demoservice:demo-admin"));
    definition.setAttributes(attributes);
    return definition;
  }

}
