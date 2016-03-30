package pdp.access;

import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.Assert.assertEquals;

public class BasicAuthenticationManagerTest {

  private BasicAuthenticationProvider manager = new BasicAuthenticationProvider("user", "password");

  @Test
  public void testAuthenticateHappyFlow() throws Exception {
    Authentication authenticate = manager.authenticate(new TestingAuthenticationToken("user", "password"));
    assertEquals("[ROLE_USER, ROLE_PEP]", authenticate.getAuthorities().toString());
  }

  @Test(expected = UsernameNotFoundException.class)
  public void testAuthenticateUsername() throws Exception {
    manager.authenticate(new TestingAuthenticationToken("unknown", "password"));
  }

  @Test(expected = BadCredentialsException.class)
  public void testAuthenticateBadCredentials() throws Exception {
    manager.authenticate(new TestingAuthenticationToken("user", "faulty"));
  }
}