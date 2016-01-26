package pdp.access;

import static org.springframework.util.Assert.notNull;
import static pdp.access.FederatedUserBuilder.apiAuthorities;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * EngineBlock and Dashboard call the PDP and we don't want to use OAuth for this as
 * they are trusted clients
 */
public class BasicAuthenticationManager implements AuthenticationManager {

  private final String userName;
  private final String password;

  public BasicAuthenticationManager(String userName, String password) {
    notNull(userName);
    notNull(password);

    this.userName = userName;
    this.password = password;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    //the exceptions are for logging and are not propagated to the end user / application
    if (!userName.equals(authentication.getPrincipal())) {
      throw new UsernameNotFoundException("Unknown user: " + authentication.getPrincipal());
    }
    if (!password.equals(authentication.getCredentials())) {
      throw new BadCredentialsException("Bad credentials");
    }
    return new UsernamePasswordAuthenticationToken(
        authentication.getPrincipal(),
        authentication.getCredentials(),
        apiAuthorities);
  }
}
