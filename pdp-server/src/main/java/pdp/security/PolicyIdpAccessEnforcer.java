package pdp.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicy;
import pdp.shibboleth.ShibbolethUser;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PolicyIdpAccessEnforcer {

  //feature toggle
  private final boolean active;

  public PolicyIdpAccessEnforcer(boolean active) {
    this.active = active;
  }

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
    if (!active || !getShibbolethUser().isPolicyIdpAccessEnforcement()) {
      return;
    }

    Assert.hasText(serviceProviderId);

    ShibbolethUser shibbolethUser = getShibbolethUser();

    String authenticatingAuthorityUser = shibbolethUser.getAuthenticatingAuthority();
    String userIdentifier = shibbolethUser.getUsername();

    Set<String> idpsOfUserEntityIds = getEntityIds(shibbolethUser.getIdpEntities());
    Set<String> spsOfUserEntityIds = getEntityIds(shibbolethUser.getSpEntities());

    if (CollectionUtils.isEmpty(identityProviderIds)) {
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

    //finally check (e.g. for update and delete actions) if the authenticatingAuthority of the policy is owned by this user
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

  private ShibbolethUser getShibbolethUser() {
    return (ShibbolethUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public String username() {
    return getShibbolethUser().getUsername();
  }

  public String authenticatingAuthority() {
    return getShibbolethUser().getAuthenticatingAuthority();
  }

  public String userDisplayName() {
    return getShibbolethUser().getDisplayName();
  }
}
