package pdp.access;

import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.Assert.assertEquals;

public class BasicAuthenticationManagerTest {

  private BasicAuthenticationManager manager = new BasicAuthenticationManager("user", "password");

  @Test
  public void testAuthenticateHappyFlow() throws Exception {
    Authentication authenticate = manager.authenticate(new TestingAuthenticationToken("user", "password"));
    assertEquals("[ROLE_USER, ROLE_PEP]", authenticate.getAuthorities().toString());
  }

  @Test
  public void testAuthenticateNonHappyFlow() throws Exception {
    Authentication authenticate = manager.authenticate(new TestingAuthenticationToken("", ""));
    assertEquals(null, authenticate);
  }

}