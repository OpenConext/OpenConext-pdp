package pdp.shibboleth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import pdp.access.FederatedUser;
import pdp.access.FederatedUserBuilder;
import pdp.access.RunAsFederatedUser;
import pdp.serviceregistry.ServiceRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static pdp.access.FederatedUserBuilder.X_IMPERSONATE;
import static pdp.access.FederatedUserBuilder.apiAuthorities;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final FederatedUserBuilder federatedUserBuilder;

  public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager, ServiceRegistry serviceRegistry) {
    super();
    setAuthenticationManager(authenticationManager);
    this.federatedUserBuilder = new FederatedUserBuilder(serviceRegistry);
    setCheckForPrincipalChanges(true);
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
    Optional<FederatedUser> federatedUser = federatedUserBuilder.shibUser(request);
    if (!federatedUser.isPresent()) {
      //null is how the contract for AbstractPreAuthenticatedProcessingFilter works
      return null;
    }
    //Now we are certain a shib admin user is logged in and we can check if there is impersonation requested
    if (hasText(request.getHeader(X_IMPERSONATE))) {
      federatedUser = federatedUserBuilder.basicAuthUser(request, new UsernamePasswordAuthenticationToken("N/A", "N/A", apiAuthorities));
    }
    return federatedUser.isPresent() ? federatedUser.get() : null;
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

  @Override
  protected boolean principalChanged(HttpServletRequest request, Authentication currentAuthentication) {
    //the Javascript client has the functionality to impersonate an user. If this functionality is off then
    //only need to check if the currentAuthentication is not a previous cached impersonation
    return hasText(request.getHeader(X_IMPERSONATE)) || currentAuthentication.getPrincipal() instanceof RunAsFederatedUser;
  }
}
