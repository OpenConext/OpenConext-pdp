package pdp.access;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import pdp.serviceregistry.ServiceRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class PolicyIdpAccessEnforcerFilter extends BasicAuthenticationFilter {

  private final FederatedUserBuilder federatedUserBuilder;

  public PolicyIdpAccessEnforcerFilter(AuthenticationManager authenticationManager, ServiceRegistry serviceRegsitry) {
    super(authenticationManager);
    this.federatedUserBuilder = new FederatedUserBuilder(serviceRegsitry);
  }

  @Override
  protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
    Optional<FederatedUser> runAsFederatedUserOptional = federatedUserBuilder.basicAuthUser(request, authResult.getAuthorities());
    if (runAsFederatedUserOptional.isPresent()) {
      SecurityContextHolder.getContext().setAuthentication(new PolicyIdpAccessAwareToken((RunAsFederatedUser) runAsFederatedUserOptional.get()));
    }
  }

}
