package pdp.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import pdp.domain.*;
import pdp.serviceregistry.ServiceRegistry;
import pdp.util.StreamUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.Assert.isInstanceOf;
import static org.springframework.util.CollectionUtils.isEmpty;
import static pdp.util.StreamUtils.singletonCollector;
import static pdp.util.StreamUtils.singletonOptionalCollector;
import static pdp.xacml.PdpPolicyDefinitionParser.IDP_ENTITY_ID;
import static pdp.xacml.PdpPolicyDefinitionParser.SP_ENTITY_ID;

public class PolicyIdpAccessEnforcer {

  private final static ObjectMapper objectMapper = new ObjectMapper();

  private final ServiceRegistry serviceRegistry;

  public PolicyIdpAccessEnforcer(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
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
    doActionAllowed(pdpPolicy, serviceProviderId, identityProviderIds, true);
  }

  public boolean actionAllowedIndicator(PdpPolicy pdpPolicy, String serviceProviderId, List<String> identityProviderIds) {
    return doActionAllowed(pdpPolicy, serviceProviderId, identityProviderIds, false);
  }

  private boolean doActionAllowed(PdpPolicy pdpPolicy, String serviceProviderId, List<String> identityProviderIds, boolean throwException) {
    FederatedUser user = federatedUser();
    if (!user.isPolicyIdpAccessEnforcementRequired()) {
      return true;
    }

    Assert.hasText(serviceProviderId);

    String authenticatingAuthorityUser = user.getAuthenticatingAuthority();
    String userIdentifier = user.getIdentifier();

    Set<String> idpsOfUserEntityIds = getEntityIds(user.getIdpEntities());
    Set<String> spsOfUserEntityIds = getEntityIds(user.getSpEntities());

    if (isEmpty(identityProviderIds)) {
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
    Set<String> spsOfUserEntityIds = getEntityIds(user.getSpEntities());

    return stream(violations.spliterator(), false).filter(violation -> maySeeViolation(violation, idpsOfUserEntityIds, spsOfUserEntityIds)).collect(toList());
  }

  /**
   * Only PdpPolicyViolation are returned where
   * <p/>
   * the Idp of the violation is owned by the user
   * <p/>
   * or the SP of the violation is owned by the user
   */
  private boolean maySeeViolation(PdpPolicyViolation violation, Set<String> idpsOfUserEntityIds,
                                  Set<String> spsOfUserEntityIds) {
    JsonPolicyRequest jsonPolicyRequest;
    try {
      //we are called from lambda
      jsonPolicyRequest = objectMapper.readValue(violation.getJsonRequest(), JsonPolicyRequest.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String idp = getEntityAttributeValue(jsonPolicyRequest, IDP_ENTITY_ID);
    String sp = getEntityAttributeValue(jsonPolicyRequest, SP_ENTITY_ID);

    return idpsOfUserEntityIds.contains(idp) || spsOfUserEntityIds.contains(sp);
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
   * <p/>
   * the IdPs of the policy are empty and the SP of the policy is allowed from through the idp of the user
   * <p/>
   * or where one of the IdPs of the policy is owned by the user
   * <p/>
   * or where the SP is owned of the policy is owned by the user.
   */
  private boolean maySeePolicy(PdpPolicyDefinition pdpPolicyDefinition, FederatedUser user,
                               Set<String> idpsOfUserEntityIds, Set<String> spsOfUserEntityIds) {
    if (isEmpty(pdpPolicyDefinition.getIdentityProviderIds())
        && idpIsAllowed(user, idpsOfUserEntityIds, pdpPolicyDefinition.getServiceProviderId())) {
      return true;
    }
    if (pdpPolicyDefinition.getIdentityProviderIds().stream().anyMatch(idp -> idpsOfUserEntityIds.contains(idp))) {
      return true;
    }
    if (spsOfUserEntityIds.contains(pdpPolicyDefinition.getServiceProviderId())) {
      return true;
    }
    return false;
  }

  private boolean idpIsAllowed(FederatedUser user, Set<String> idpsOfUserEntityIds, String serviceProviderId) {
    boolean isAllowedFromIdp = user.getIdpEntities().stream().anyMatch(idp -> idp.isAllowedFrom(serviceProviderId));
    //rare case to check: ACLs are mostly defined on IdPs, but the SP can also have an ACL to restrict IDPs
    Optional<EntityMetaData> spOptional = serviceRegistry.serviceProviders().stream().filter(metaData -> metaData.getEntityId().equals(serviceProviderId)).collect(singletonOptionalCollector());
    String[] idps = idpsOfUserEntityIds.toArray(new String[idpsOfUserEntityIds.size()]);
    return isAllowedFromIdp && spOptional.isPresent() && spOptional.get().isAllowedFrom(idps);
  }

  private Set<String> getEntityIds(Set<EntityMetaData> entities) {
    return entities.stream().map(EntityMetaData::getEntityId).collect(toSet());
  }

  private FederatedUser federatedUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    isInstanceOf(FederatedUser.class, principal);
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

}
