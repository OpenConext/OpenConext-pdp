package pdp.shibboleth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;
import pdp.access.FederatedUser;
import pdp.domain.EntityMetaData;
import pdp.serviceregistry.ServiceRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Set;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

  public static final String UID_HEADER_NAME = "uid";
  public static final String DISPLAY_NAME_HEADER_NAME = "displayname";
  public static final String IS_MEMBER_OF = "is-member-of";
  public static final String SHIB_AUTHENTICATING_AUTHORITY = "Shib-Authenticating-Authority";

  private static final Logger LOG = LoggerFactory.getLogger(ShibbolethPreAuthenticatedProcessingFilter.class);
  private static final Collection<? extends GrantedAuthority> authorities = createAuthorityList("ROLE_USER", "ROLE_ADMIN");

  private final ServiceRegistry serviceRegsitry;

  public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager, ServiceRegistry serviceRegistry) {
    super();
    setAuthenticationManager(authenticationManager);
    this.serviceRegsitry = serviceRegistry;
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
    String uid = request.getHeader(UID_HEADER_NAME);
    String displayName = request.getHeader(DISPLAY_NAME_HEADER_NAME);
    String authenticatingAuthority = request.getHeader(SHIB_AUTHENTICATING_AUTHORITY);

    if (isHeaderValueInvalid(uid, UID_HEADER_NAME)) {
      return null;
    }
    if (isHeaderValueInvalid(displayName, DISPLAY_NAME_HEADER_NAME)) {
      return null;
    }
    if (isHeaderValueInvalid(authenticatingAuthority, SHIB_AUTHENTICATING_AUTHORITY)) {
      return null;
    }

    //By contract we always get at least one Idp, but in case there are two - http://mock-idp;http://mock-idp - we need the first
    authenticatingAuthority = authenticatingAuthority.split(";")[0];
    Set<EntityMetaData> idpEntities = serviceRegsitry.identityProvidersByAuthenticatingAuthority(authenticatingAuthority);

    //By contract we have at least one Idp - otherwise an Exception is already raised
    String institutionId = idpEntities.iterator().next().getInstitutionId();
    Set<EntityMetaData> spEntities = serviceRegsitry.serviceProvidersByInstitutionId(institutionId);

    LOG.debug("Creating ShibbolethUser {}",uid);
    return new FederatedUser(uid, authenticatingAuthority, displayName, idpEntities, spEntities, authorities);
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

  private boolean isHeaderValueInvalid(String header, String name) {
    if (StringUtils.isEmpty(header)) {
      LOG.warn("Missing {} header. Not possible to login", name);
      return true;
    }
    return false;
  }
}
