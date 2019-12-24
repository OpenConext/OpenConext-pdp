package pdp.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import pdp.JsonMapper;
import pdp.domain.EntityMetaData;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.manage.Manage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static pdp.access.PolicyAccess.READ;
import static pdp.access.PolicyAccess.VIOLATIONS;
import static pdp.util.StreamUtils.singletonCollector;
import static pdp.xacml.PdpPolicyDefinitionParser.IDP_ENTITY_ID;

public class PolicyIdpAccessEnforcer implements JsonMapper {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyIdpAccessEnforcer.class);

    /**
     * Create, update or delete actions and access to the (read-only) revisions are only allowed if the
     * AuthenticatingAuthority of the signed in user equals the AuthenticatingAuthority of the PdpPolicy or the
     * AuthenticatingAuthority of the user is linked (through the InstitutionID) to the AuthenticatingAuthority of
     * the PdpPolicy.
     * <p>
     * The CUD actions are also only allowed if all of the Idps of the pdpPolicy equal or are linked to the
     * AuthenticatingAuthority of the signed in user.
     * <p>
     * If the Idp list of the policy is empty then the SP must have the same institutionID as the institutionID of the
     * AuthenticatingAuthority of the signed in user.
     * <p>
     * Violations can only be seen if the IdP of the JSON request is equal or linked to the AuthenticatingAuthority of
     * the signed in user.
     */
    public void actionAllowed(PdpPolicy pdpPolicy, PolicyAccess policyAccess, String serviceProviderId, List<String> identityProviderIds) {
        doActionAllowed(pdpPolicy, policyAccess, serviceProviderId, identityProviderIds, true);
    }

    public boolean actionAllowedIndicator(PdpPolicy pdpPolicy, PolicyAccess policyAccess, String serviceProviderId, List<String> identityProviderIds) {
        return doActionAllowed(pdpPolicy, policyAccess, serviceProviderId, identityProviderIds, false);
    }

    private boolean doActionAllowed(PdpPolicy pdpPolicy, PolicyAccess policyAccess, String serviceProviderId, List<String> identityProviderIds, boolean throwException) {
        FederatedUser user = federatedUser();
        if (!user.isPolicyIdpAccessEnforcementRequired()) {
            return true;
        }

        notNull(policyAccess, "PolicyAccess must not be null");

        if (policyAccess.equals(VIOLATIONS)) {
            //No way of telling based on the pdpPolicy and violations are filtered later
            return true;
        }

        hasText(serviceProviderId, "ServiceProvider ID must have text");

        String authenticatingAuthorityUser = user.getAuthenticatingAuthority();
        String userIdentifier = user.getIdentifier();

        Set<String> idpsOfUserEntityIds = getEntityIds(user.getIdpEntities());
        Set<String> spsOfUserEntityIds = getEntityIds(user.getSpEntities());

        if (isEmpty(identityProviderIds)) {
            switch (policyAccess) {
                case READ:
                    //Valid to have no identityProvidersIds, but then the SP must be allowed access by this users IdP
                    if (!idpIsAllowed(user, idpsOfUserEntityIds, serviceProviderId)) {
                        if (throwException) {
                            throw new PolicyIdpAccessMismatchServiceProviderException(String.format(
                                "Policy for target SP '%s' requested by '%s', but this SP is not allowed access by users from IdP '%s'",
                                serviceProviderId,
                                userIdentifier,
                                authenticatingAuthorityUser)
                            );
                        }
                        return false;
                    }
                    break;
                case WRITE:
                    //Valid to have no identityProvidersIds, but then the SP must be linked by this users IdP
                    if (!spsOfUserEntityIds.contains(serviceProviderId)) {
                        if (throwException) {
                            throw new PolicyIdpAccessMismatchServiceProviderException(String.format(
                                "Policy for target SP '%s' requested by '%s', but this SP is not linked to users IdP '%s'",
                                serviceProviderId,
                                userIdentifier,
                                authenticatingAuthorityUser)
                            );
                        }
                        return false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Not handled PolicyAccess " + policyAccess);
            }
        } else {
            //now the SP may be anything, however all selected IDPs for this policy must be linked to this users IDP
            if (!idpsOfUserEntityIds.containsAll(identityProviderIds)) {
                if (throwException) {
                    throw new PolicyIdpAccessMismatchIdentityProvidersException(String.format(
                        "Policy for target IdPs '%s' requested by '%s', but not all are linked to users IdP '%s",
                        identityProviderIds,
                        userIdentifier,
                        authenticatingAuthorityUser)
                    );
                }
                return false;
            }

        }
        if (policyAccess.equals(READ)) {
            //Revisions may be seen when we get to this point
            return true;
        }

        //finally check (e.g. for update and delete actions) if the getAuthenticatingAuthority of the policy is owned by this user
        String authenticatingAuthorityPolicy = pdpPolicy.getAuthenticatingAuthority();
        if (!authenticatingAuthorityPolicy.equals(authenticatingAuthorityUser) &&
            !idpsOfUserEntityIds.contains(authenticatingAuthorityPolicy)) {
            if (throwException) {
                throw new PolicyIdpAccessOriginatingIdentityProviderException(String.format(
                    "Policy created by admin '%s' of IdP '%s' can not be updated / deleted by admin '%s' of IdP '%s'",
                    pdpPolicy.getUserIdentifier(),
                    authenticatingAuthorityPolicy,
                    userIdentifier,
                    authenticatingAuthorityUser)
                );
            }
            return false;
        }
        return true;
    }

    /**
     * If the logged in FederatedUser requires policyIdpAccessEnforcement then only those violations are
     * returned which the user may see
     */
    public Iterable<PdpPolicyViolation> filterViolations(Iterable<PdpPolicyViolation> violations) {
        FederatedUser user = federatedUser();
        if (!user.isPolicyIdpAccessEnforcementRequired()) {
            return violations;
        }
        Set<String> idpsOfUserEntityIds = getEntityIds(user.getIdpEntities());

        return stream(violations.spliterator(), false).filter(violation -> maySeeViolation(violation, idpsOfUserEntityIds)).collect(toList());
    }

    /**
     * Only PdpPolicyViolation are returned where the Idp of the violation is owned by the user
     */
    private boolean maySeeViolation(PdpPolicyViolation violation, Set<String> idpsOfUserEntityIds) {
        JsonPolicyRequest jsonPolicyRequest;
        try {
            //we are called from lambda
            jsonPolicyRequest = objectMapper.readValue(violation.getJsonRequest(), JsonPolicyRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String idp = getEntityAttributeValue(jsonPolicyRequest, IDP_ENTITY_ID);

        return idpsOfUserEntityIds.contains(idp);
    }

    private String getEntityAttributeValue(JsonPolicyRequest jsonPolicyRequest, String attributeName) {
        return jsonPolicyRequest.request.resource.attributes.stream()
            .filter(attr -> attr.attributeId.equals(attributeName))
            .collect(singletonCollector()).value;
    }

    /**
     * If the logged in FederatedUser requires policyIdpAccessEnforcement then only those PdpPolicyDefinitions are
     * returned which the user may see
     */
    public List<PdpPolicyDefinition> filterPdpPolicies(List<PdpPolicyDefinition> policies) {
        FederatedUser user = federatedUser();
        if (!user.isPolicyIdpAccessEnforcementRequired()) {
            return policies;
        }
        Set<String> idpsOfUserEntityIds = getEntityIds(user.getIdpEntities());
        Set<String> spsOfUserEntityIds = getEntityIds(user.getSpEntities());
        return policies.stream().filter(policy -> maySeePolicy(policy, user, idpsOfUserEntityIds, spsOfUserEntityIds)).collect(toList());
    }

    /**
     * Only PdpPolicyDefinitions are returned where
     * <p>
     * the IdPs of the policy are empty and the SP of the policy is allowed from through the idp of the user
     * <p>
     * or where one of the IdPs of the policy is owned by the user
     * <p>
     * the IdPs of the policy are empty and the SP of the policy is owned by the user
     */
    private boolean maySeePolicy(PdpPolicyDefinition pdpPolicyDefinition, FederatedUser user,
                                 Set<String> idpsOfUserEntityIds, Set<String> spsOfUserEntityIds) {
        List<String> identityProviderIds = pdpPolicyDefinition.getIdentityProviderIds();

        if (isEmpty(identityProviderIds) &&
            (idpIsAllowed(user, idpsOfUserEntityIds, pdpPolicyDefinition.getServiceProviderId())
                || spsOfUserEntityIds.contains(pdpPolicyDefinition.getServiceProviderId()))) {
            return true;
        } else if (identityProviderIds.stream().anyMatch(idp -> idpsOfUserEntityIds.contains(idp))) {
            return true;
        }
        return false;
    }

    private boolean idpIsAllowed(FederatedUser user, Set<String> idpsOfUserEntityIds, String serviceProviderId) {
        boolean isAllowedFromIdp = user.getIdpEntities().stream().anyMatch(idp -> idp.isAllowedFrom(serviceProviderId));
        return isAllowedFromIdp;
    }

    public List<EntityMetaData> filterIdentityProviders(List<EntityMetaData> identityProviders) {
        FederatedUser user = federatedUser();
        if (!user.isPolicyIdpAccessEnforcementRequired()) {
            return identityProviders;
        }
        Set<String> idpsOfUserEntityIds = getEntityIds(user.getIdpEntities());

        return identityProviders.stream().filter(idp -> idpsOfUserEntityIds.contains(idp.getEntityId())).collect(toList());
    }

    private Set<String> getEntityIds(Set<EntityMetaData> entities) {
        return entities.stream().map(EntityMetaData::getEntityId).collect(toSet());
    }

    public static FederatedUser federatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!FederatedUser.class.isAssignableFrom(principal.getClass())) {
            LOG.warn("Could not find federated user, but {}", principal);
            throw new PolicyIdpAccessMissingFederatedUserException("Could not find authenticated federated user");
        }

        return (FederatedUser) principal;
    }

    public String username() {
        return federatedUser().getIdentifier();
    }

    public String authenticatingAuthority() {
        return federatedUser().getAuthenticatingAuthority();
    }

    public String userDisplayName() {
        return federatedUser().getDisplayName();
    }

    public boolean isPolicyIdpAccessEnforcementRequired() {
        return federatedUser().isPolicyIdpAccessEnforcementRequired();
    }
}
