package pdp.shibboleth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;
import pdp.serviceregistry.ServiceRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

  public static final String UID_HEADER_NAME = "uid";
  public static final String DISPLAY_NAME_HEADER_NAME = "displayname";
  public static final String IS_MEMBER_OF = "is-member-of";
  public static final String SHIB_AUTHENTICATING_AUTHORITY = "Shib-Authenticating-Authority";

  private static final Logger LOG = LoggerFactory.getLogger(ShibbolethPreAuthenticatedProcessingFilter.class);

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
    String isMemberOf = request.getHeader(IS_MEMBER_OF);
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

    String role = StringUtils.hasText(isMemberOf) ? "PAP_ADMIN" : "PAP_CLIENT";

    Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(role));

    //By contract we always get at least one Idp, but in case there are two - http://mock-idp;http://mock-idp - we need the first
    authenticatingAuthority = authenticatingAuthority.split(";")[0];
    Set<EntityMetaData> idpEntities = serviceRegsitry.identityProvidersByAuthenticatingAuthority(authenticatingAuthority);

    String institutionId = idpEntities.stream().findAny().get().getInstitutionId();
    Set<EntityMetaData> spEntities = serviceRegsitry.serviceProvidersByInstitutionId(institutionId);
    LOG.debug("Creating ShibbolethUser {}",uid);
    return new ShibbolethUser(uid, displayName, idpEntities, spEntities, authorities);
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
