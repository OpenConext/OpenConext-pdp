package pdp.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import pdp.domain.EntityMetaData;
import pdp.serviceregistry.ServiceRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.util.StringUtils.isEmpty;

public class FederatedUserBuilder {

  private static final Collection<? extends GrantedAuthority> shibAuthorities = createAuthorityList("ROLE_USER", "ROLE_ADMIN");

  public static final Collection<? extends GrantedAuthority> apiAuthorities = createAuthorityList("ROLE_USER", "ROLE_PEP");

  //shib headers
  public static final String UID_HEADER_NAME = "uid";
  public static final String DISPLAY_NAME_HEADER_NAME = "displayname";
  public static final String SHIB_AUTHENTICATING_AUTHORITY = "Shib-Authenticating-Authority";

  //trusted API headers
  public static final String X_IDP_ENTITY_ID = "X-IDP-ENTITY-ID";
  public static final String X_UNSPECIFIED_NAME_ID = "X-UNSPECIFIED-NAME-ID";
  public static final String X_DISPLAY_NAME = "X-DISPLAY-NAME";

  //impersonate header
  public static final String X_IMPERSONATE = "X-IMPERSONATE";

  private static final Logger LOG = LoggerFactory.getLogger(FederatedUserBuilder.class);

  private final ServiceRegistry serviceRegsitry;

  public FederatedUserBuilder(ServiceRegistry serviceRegsitry) {
    this.serviceRegsitry = serviceRegsitry;
  }

  public Optional<FederatedUser> basicAuthUser(HttpServletRequest request, Authentication authResult) {
    //check headers for enrichment of the Authentication
    String idpEntityId = request.getHeader(X_IDP_ENTITY_ID);
    String nameId = request.getHeader(X_UNSPECIFIED_NAME_ID);
    String displayName = request.getHeader(X_DISPLAY_NAME);

    if (isEmpty(idpEntityId) || isEmpty(nameId) || isEmpty(displayName)) {
      //any policy idp access checks will fail, but it might be that this call is not for something that requires access
      return Optional.empty();
    }

    Set<EntityMetaData> idpEntities = serviceRegsitry.identityProvidersByAuthenticatingAuthority(idpEntityId);
    Set<EntityMetaData> spEntities = getSpEntities(idpEntities);

    LOG.debug("Creating RunAsFederatedUser {}",nameId);
    return Optional.of(new RunAsFederatedUser(nameId, idpEntityId, displayName, idpEntities, spEntities, authResult.getAuthorities()));

  }

  public Optional<FederatedUser> shibUser(HttpServletRequest request) {
    String uid = request.getHeader(UID_HEADER_NAME);
    String displayName = request.getHeader(DISPLAY_NAME_HEADER_NAME);
    String authenticatingAuthority = request.getHeader(SHIB_AUTHENTICATING_AUTHORITY);

    if (isEmpty(uid) || isEmpty(displayName) || isEmpty(authenticatingAuthority)) {
      return Optional.empty();
    }

    //By contract we always get at least one Idp and usually two separated by a semi-colon
    authenticatingAuthority = authenticatingAuthority.split(";")[0];
    Set<EntityMetaData> idpEntities = serviceRegsitry.identityProvidersByAuthenticatingAuthority(authenticatingAuthority);

    //By contract we have at least one Idp - otherwise an Exception is already raised
    Set<EntityMetaData> spEntities = getSpEntities(idpEntities);

    LOG.debug("Creating FederatedUser {}",uid);
    return Optional.of(new FederatedUser(uid, authenticatingAuthority, displayName, idpEntities, spEntities, shibAuthorities));

  }

  private Set<EntityMetaData> getSpEntities(Set<EntityMetaData> idpEntities) {
    //By contract we have at least one Idp - otherwise an Exception is already raised
    String institutionId = idpEntities.iterator().next().getInstitutionId();
    return serviceRegsitry.serviceProvidersByInstitutionId(institutionId);
  }

}
