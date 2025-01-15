package pdp.policies;

import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicyDefinition;
import pdp.mail.MailBox;
import pdp.manage.Manage;
import pdp.repositories.PdpPolicyRepository;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.stream.Collectors.toList;

public class PolicyMissingServiceProviderValidator {

    private MailBox mailBox;
    private Manage manage;
    private PdpPolicyRepository pdpPolicyRepository;
    private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();

    public PolicyMissingServiceProviderValidator(MailBox mailBox, Manage manage,
                                                 PdpPolicyRepository pdpPolicyRepository,
                                                 boolean pdpCronJobResponsible) {
        this.mailBox = mailBox;
        this.manage = manage;
        this.pdpPolicyRepository = pdpPolicyRepository;
        if (pdpCronJobResponsible) {
            newScheduledThreadPool(1).scheduleAtFixedRate(() -> this.validate(), 1, 7 * 24, TimeUnit.HOURS);
        }
    }

    public List<PdpPolicyDefinition> addEntityMetaData(List<PdpPolicyDefinition> pdpPolicyDefinitions) {
        Set<String> idpEntitiesIds = pdpPolicyDefinitions.stream()
                .map(PdpPolicyDefinition::getIdentityProviderIds)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        idpEntitiesIds.addAll(pdpPolicyDefinitions.stream().map(PdpPolicyDefinition::getAuthenticatingAuthorityName).collect(Collectors.toSet()));
        Map<String, EntityMetaData> identityProviders = manage.identityProvidersByEntityIds(idpEntitiesIds);

        Set<String> spEntitiesIds = pdpPolicyDefinitions.stream()
                .map(PdpPolicyDefinition::getServiceProviderIds)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        Map<String, EntityMetaData> serviceProviders = manage.serviceProvidersByEntityIds(spEntitiesIds);

        pdpPolicyDefinitions.forEach(pd -> {
            EntityMetaData emd = identityProviders.get(pd.getAuthenticatingAuthorityName());
            if (emd != null) {
                pd.setAuthenticatingAuthorityName(emd.getNameEn());
            }

            List<String> spEntityIds = pd.getServiceProviderIds();
            List<EntityMetaData> policyServiceProviderMetaData = serviceProviders.values().stream()
                    .filter(sp -> spEntityIds.contains(sp.getEntityId()))
                    .collect(toList());
            pd.setServiceProviderNames(policyServiceProviderMetaData.stream()
                    .map(sp -> sp.getNameEn())
                    .collect(toList()));
            pd.setServiceProviderNamesNl(policyServiceProviderMetaData.stream()
                    .map(sp -> sp.getNameNl())
                    .collect(toList()));
            pd.setServiceProviderInvalidOrMissing(spEntityIds.stream().noneMatch(spEntityId -> serviceProviders.containsKey(spEntityId)));
            pd.setActivatedSr(policyServiceProviderMetaData.stream().allMatch(sp -> sp.isPolicyEnforcementDecisionRequired()));

            List<String> identityProviderIds = pd.getIdentityProviderIds();
            pd.setIdentityProviderNames(identityProviders.values().stream()
                    .filter(idp -> identityProviderIds.contains(idp.getEntityId()))
                    .map(idp -> idp.getNameEn())
                    .collect(toList()));
            pd.setIdentityProviderNamesNl(identityProviders.values().stream()
                    .filter(idp -> identityProviderIds.contains(idp.getEntityId()))
                    .map(idp -> idp.getNameNl())
                    .collect(toList()));
        });


        return pdpPolicyDefinitions;
    }

    public void validate() {
        List<PdpPolicyDefinition> pdpPolicyDefinitions = pdpPolicyRepository.findAll().stream().map(policy -> pdpPolicyDefinitionParser.parse(policy)).collect(toList());
        List<PdpPolicyDefinition> invalidPolicies = this.addEntityMetaData(pdpPolicyDefinitions).stream()
                .filter(policy -> policy.isServiceProviderInvalidOrMissing())
                .filter(policy -> policy.isActive())
                .collect(toList());

        if (!invalidPolicies.isEmpty()) {
            this.mailBox.sendInvalidPoliciesMail(invalidPolicies);
        }
    }
}
