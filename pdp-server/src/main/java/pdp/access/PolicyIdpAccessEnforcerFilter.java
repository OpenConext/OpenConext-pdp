package pdp.access;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;
import pdp.serviceregistry.ServiceRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class PolicyIdpAccessEnforcerFilter extends BasicAuthenticationFilter {

  public static final String X_IDP_ENTITY_ID = "X-IDP-ENTITY-ID";
  public static final String X_UNSPECIFIED_NAME_ID = "X-UNSPECIFIED-NAME-ID";
  public static final String X_DISPLAY_NAME = "X-DISPLAY-NAME";

  private final ServiceRegistry serviceRegsitry;

  public PolicyIdpAccessEnforcerFilter(AuthenticationManager authenticationManager, ServiceRegistry serviceRegsitry) {
    super(authenticationManager);
    this.serviceRegsitry = serviceRegsitry;
  }

  @Override
  protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
    //check headers for enrichment of the Authentication
    String idpEntityId = request.getHeader(X_IDP_ENTITY_ID);
    String nameId = request.getHeader(X_UNSPECIFIED_NAME_ID);
    String displayName = request.getHeader(X_DISPLAY_NAME);

    if (!StringUtils.hasText(idpEntityId) || !StringUtils.hasText(nameId) || !StringUtils.hasText(displayName)) {
      //any policy idp access checks will fail, but it might be that this call is not for something that requires access
      return;
    }

    Set<EntityMetaData> idpEntities = serviceRegsitry.identityProvidersByAuthenticatingAuthority(idpEntityId);

    //By contract we have at least one Idp - otherwise an Exception is already raised
    String institutionId = idpEntities.iterator().next().getInstitutionId();
    Set<EntityMetaData> spEntities = serviceRegsitry.serviceProvidersByInstitutionId(institutionId);

    RunAsFederatedUser policyIdpAccessAwarePrincipal = new RunAsFederatedUser(nameId, idpEntityId, displayName, idpEntities, spEntities, authResult.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(new PolicyIdpAccessAwareToken(policyIdpAccessAwarePrincipal));

  }

}
