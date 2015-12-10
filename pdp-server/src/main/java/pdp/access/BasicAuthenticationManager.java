package pdp.access;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

/**
 * EngineBlock and Dashboard call the PDP and we don't want to use OAuth for this as
 * they are trusted clients
 */
public class BasicAuthenticationManager implements AuthenticationManager {

  private static final List<GrantedAuthority> authorities = createAuthorityList("ROLE_USER", "ROLE_PEP");

  private final String userName;
  private final String password;

  public BasicAuthenticationManager(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (authentication.getPrincipal().equals(userName)
        && authentication.getCredentials().equals(password)) {
      return new UsernamePasswordAuthenticationToken(
          authentication.getPrincipal(),
          authentication.getCredentials(),
          authorities);
    }
    return null;
  }
}
