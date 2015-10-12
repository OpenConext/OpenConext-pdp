package pdp.shibboleth;

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

    Collection<GrantedAuthority> authorities = StringUtils.hasText(isMemberOf) ? Arrays.asList(new SimpleGrantedAuthority("PAP_CLIENT")) : Collections.EMPTY_LIST;

    //By contract we always get at least one Idp, but in case there are two - http://mock-idp;http://mock-idp - we need the first
    //it can happen that the Authenticating Authority looks like this: http://mock-idp;http://mock-idp
    authenticatingAuthority = authenticatingAuthority.split(";")[0];
    Set<EntityMetaData> idpEntities = serviceRegsitry.identityProvidersByAuthenticatingAuthority(authenticatingAuthority);

    String institutionId = idpEntities.stream().findAny().get().getInstitutionId();
    Set<EntityMetaData> spEntities = serviceRegsitry.serviceProvidersByInstitutionId(institutionId);

    return StringUtils.hasText(uid) && StringUtils.hasText(displayName) ?
        new ShibbolethUser(uid, displayName, idpEntities, spEntities, authorities) : null;
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }
}
