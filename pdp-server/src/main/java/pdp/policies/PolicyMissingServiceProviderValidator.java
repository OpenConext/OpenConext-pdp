package pdp.policies;

import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.mail.MailBox;
import pdp.manage.Manage;
import pdp.repositories.PdpPolicyRepository;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class PolicyMissingServiceProviderValidator {

    private MailBox mailBox;
    private Manage manage;
    private PdpPolicyRepository pdpPolicyRepository;
    private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();

    public PolicyMissingServiceProviderValidator(MailBox mailBox, Manage manage,
                                                 PdpPolicyRepository pdpPolicyRepository) {
        this.mailBox = mailBox;
        this.manage = manage;
        this.pdpPolicyRepository = pdpPolicyRepository;
        this.validate();
    }

    public PdpPolicyDefinition addEntityMetaData(PdpPolicyDefinition pd) {
        Optional<EntityMetaData> sp = manage.serviceProviderOptionalByEntityId(pd.getServiceProviderId());
        pd.setServiceProviderInvalidOrMissing(!sp.isPresent());
        if (sp.isPresent()) {
            pd.setServiceProviderName(sp.get().getNameEn());
            pd.setServiceProviderNameNl(sp.get().getNameNl());
            pd.setActivatedSr(sp.get().isPolicyEnforcementDecisionRequired());
        }
        manage.enrichPdPPolicyDefinition(pd);
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
        }
    }
}
