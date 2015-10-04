package pdp.shibboleth;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class ShibbolethUserDetailService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

  @Override
  public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authentication) throws UsernameNotFoundException {
    return (ShibbolethUser) authentication.getPrincipal();
  }
}
