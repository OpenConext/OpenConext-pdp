package pdp.access;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class PolicyIdpAccessAwareToken extends AbstractAuthenticationToken {

  private RunAsFederatedUser policyIdpAccessAwarePrincipal;

  public PolicyIdpAccessAwareToken(RunAsFederatedUser policyIdpAccessAwarePrincipal) {
    super(policyIdpAccessAwarePrincipal.getAuthorities());
    this.policyIdpAccessAwarePrincipal = policyIdpAccessAwarePrincipal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return "N/A";
  }

  @Override
  public Object getPrincipal() {
    return policyIdpAccessAwarePrincipal;
  }
}
