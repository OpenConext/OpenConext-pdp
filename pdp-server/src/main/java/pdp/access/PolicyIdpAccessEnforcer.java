package pdp.access;

import org.springframework.security.core.context.SecurityContextHolder;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicy;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PolicyIdpAccessEnforcer {

  /**
   * Create, update or delete are only allowed if the AuthenticatingAuthority of the signed in user equals the
   * AuthenticatingAuthority of the PdpPolicy or the AuthenticatingAuthority of the user is linked (through the
   * InstitutionID) to the AuthenticatingAuthority of the PdpPolicy.
   * <p/>
   * The CUD actions are also only allowed if all of the Idps of the pdpPolicy equal or are linked to the
   * AuthenticatingAuthority of the signed in user.
   * <p/>
   * If the Idp list of the policy is empty then the SP must have the same institutionID as the institutionID of the
   * AuthenticatingAuthority of the signed in user.
   */
  public void actionAllowed(PdpPolicy pdpPolicy, String serviceProviderId, List<String> identityProviderIds) {
    FederatedUser user = federatedUser();
    if (!user.isPolicyIdpAccessEnforcementRequired()) {
      return;
    }

    hasText(serviceProviderId);

    String authenticatingAuthorityUser = user.getAuthenticatingAuthority();
    String userIdentifier = user.getIdentifier();

    Set<String> idpsOfUserEntityIds = getEntityIds(user.getIdpEntities());
    Set<String> spsOfUserEntityIds = getEntityIds(user.getSpEntities());

    if (isEmpty(identityProviderIds)) {
      //Valid to have no identityProvidersIds, but then the SP must be linked by this users IdP
      if (!spsOfUserEntityIds.contains(serviceProviderId)) {
        throw new PolicyIdpAccessMismatchServiceProviderException(String.format(
            "Policy for target SP '%s' requested by '%s', but this SP is not linked to users IdP '%s'",
            serviceProviderId,
            userIdentifier,
            authenticatingAuthorityUser)
        );
      }
    } else {
      //now the SP may be anything, however all selected IDPs for this policy must be linked to this users IDP
      if (!idpsOfUserEntityIds.containsAll(identityProviderIds)) {
        throw new PolicyIdpAccessMismatchIdentityProvidersException(String.format(
            "Policy for target IdPs '%s' requested by '%s', but not all are linked to users IdP '%s",
            identityProviderIds,
            userIdentifier,
            authenticatingAuthorityUser)
        );
      }
    }

    //finally check (e.g. for update and delete actions) if the getAuthenticatingAuthority of the policy is owned by this user
    String authenticatingAuthorityPolicy = pdpPolicy.getAuthenticatingAuthority();
    if (!authenticatingAuthorityPolicy.equals(authenticatingAuthorityUser) &&
        !idpsOfUserEntityIds.contains(authenticatingAuthorityPolicy)) {
      throw new PolicyIdpAccessOriginatingIdentityProviderException(String.format(
          "Policy created by admin '%s' of IdP '%s' can not be updated / deleted by admin '%s' of IdP '%s'",
          pdpPolicy.getUserIdentifier(),
          authenticatingAuthorityPolicy,
          userIdentifier,
          authenticatingAuthorityUser)
      );
    }
  }

  private Set<String> getEntityIds(Set<EntityMetaData> idpEntities) {
    return idpEntities.stream().map(EntityMetaData::getEntityId).collect(toSet());
  }

  private FederatedUser federatedUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof FederatedUser) {
      return (FederatedUser) principal;
    }
    throw new RuntimeException("Principal is not FederatedUser. " + principal.getClass().getName());
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
}
