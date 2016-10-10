package pdp.policies;

import java.util.List;
import java.util.Optional;

import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.mail.MailBox;
import pdp.repositories.PdpPolicyRepository;
import pdp.serviceregistry.ServiceRegistry;
import pdp.xacml.PdpPolicyDefinitionParser;

import static java.util.stream.StreamSupport.stream;
import static java.util.stream.Collectors.toList;

public class PolicyMissingServiceProviderValidator {
  private MailBox mailBox;
  private ServiceRegistry serviceRegistry;
  private PdpPolicyRepository pdpPolicyRepository;
  private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();

  public PolicyMissingServiceProviderValidator(MailBox mailBox, ServiceRegistry serviceRegistry,
      PdpPolicyRepository pdpPolicyRepository) {
    this.mailBox = mailBox;
    this.serviceRegistry = serviceRegistry;
    this.pdpPolicyRepository = pdpPolicyRepository;
  }
  
  private PdpPolicyDefinition addEntityMetaData(PdpPolicyDefinition pd) {
    Optional<EntityMetaData> sp = serviceRegistry.serviceProviderOptionalByEntityId(pd.getServiceProviderId());
    pd.setServiceProviderInvalidOrMissing(!sp.isPresent());
    if (sp.isPresent()) {
      pd.setServiceProviderName(sp.get().getNameEn());
      pd.setActivatedSr(sp.get().isPolicyEnforcementDecisionRequired());
    }
    pd.setIdentityProviderNames(serviceRegistry.identityProviderNames(pd.getIdentityProviderIds()));
    return pd;
  }
  
  public void validate() {
    List<PdpPolicyDefinition> invalidPolicies = stream(pdpPolicyRepository.findAll().spliterator(), false)
        .map(policy -> addEntityMetaData(pdpPolicyDefinitionParser.parse(policy)))
        .filter(policy -> policy.isServiceProviderInvalidOrMissing())
        .filter(policy -> policy.isActive())
        .collect(toList());
    
    if (!invalidPolicies.isEmpty()) {
      this.mailBox.sendInvalidPoliciesMail(invalidPolicies);
      
      invalidPolicies.forEach(policyDefinition -> {
        PdpPolicy pdpPolicy = pdpPolicyRepository.findOne(policyDefinition.getId());
        pdpPolicy.setActive(false);
        pdpPolicyRepository.save(pdpPolicy);
      });
    }
  }
}
