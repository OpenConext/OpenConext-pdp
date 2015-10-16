package pdp.policies;

import org.springframework.core.io.ByteArrayResource;
import pdp.PolicyTemplateEngine;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.serviceregistry.ServiceRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class PerformancePrePolicyLoader extends DevelopmentPrePolicyLoader {

  private final ServiceRegistry serviceRegistry;
  private final PolicyTemplateEngine templateEngine = new PolicyTemplateEngine();
  private final int count;

  public PerformancePrePolicyLoader(int count, ServiceRegistry serviceRegistry, PdpPolicyRepository pdpPolicyRepository, PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    super(new ByteArrayResource("noop".getBytes()), pdpPolicyRepository, pdpPolicyViolationRepository);
    this.count = count;
    this.serviceRegistry = serviceRegistry;
  }

  @Override
  public List<PdpPolicy> getPolicies() {
    // for every ServiceProvider create a policy
    List<EntityMetaData> sps = serviceRegistry.serviceProviders();
    List<EntityMetaData> idps = serviceRegistry.identityProviders();
    EntityMetaData idp = idps.get(idps.size() - 1);
    int nbr = (this.count == 0 ? sps.size() : this.count);
    return sps.subList(0, nbr).stream(). map(sp -> pdpPolicyDefinition(sp, idp, UUID.randomUUID().toString()))
        .map(def -> new PdpPolicy(templateEngine.createPolicyXml(def), def.getName()))
        .collect(toList());
  }

  private PdpPolicyDefinition pdpPolicyDefinition(EntityMetaData sp,EntityMetaData idp, String uuid) {
    PdpPolicyDefinition definition = new PdpPolicyDefinition();
    definition.setName("Performance_Policy_" + uuid);
    definition.setDescription("Performance Policy " + uuid);
    definition.setServiceProviderId(sp.getEntityId());
    definition.setDenyAdvice("Not authorized");
    definition.setDenyAdviceNl("Niet geautoriseerd");
    definition.setDescription("Performance Policy " + uuid);
    definition.setIdentityProviderIds(Arrays.asList(idp.getEntityId()));
    List<PdpAttribute> attributes = Arrays.asList(
        new PdpAttribute("urn:mace:dir:attribute-def:eduPersonAffiliation", "teacher"),
        new PdpAttribute("urn:mace:dir:attribute-def:eduPersonAffiliation", "staff"),
        new PdpAttribute("urn:mace:dir:attribute-def:eduPersonEntitlement", "urn:mace:example.org:demoservice:demo-admin"));
    definition.setAttributes(attributes);
    return definition;
  }

}
