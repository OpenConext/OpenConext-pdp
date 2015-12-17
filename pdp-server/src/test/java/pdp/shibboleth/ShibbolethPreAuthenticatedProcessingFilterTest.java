package pdp.shibboleth;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import pdp.access.FederatedUser;
import pdp.policies.PolicyLoader;
import pdp.serviceregistry.ClassPathResourceServiceRegistry;
import pdp.serviceregistry.TestingServiceRegistry;
import pdp.shibboleth.mock.MockShibbolethFilter;

import static org.junit.Assert.assertEquals;
import static pdp.access.FederatedUserBuilder.*;

public class ShibbolethPreAuthenticatedProcessingFilterTest {

  private final static ShibbolethPreAuthenticatedProcessingFilter filter = new ShibbolethPreAuthenticatedProcessingFilter(null, new TestingServiceRegistry());

  @Test
  public void testGetPreAuthenticatedPrincipal() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    request.addHeader(UID_HEADER_NAME, "urn:collab:person:example.com:admin");
    request.addHeader(SHIB_AUTHENTICATING_AUTHORITY, PolicyLoader.authenticatingAuthority);
    request.addHeader(DISPLAY_NAME_HEADER_NAME, "John Doe");

    FederatedUser principal = (FederatedUser) filter.getPreAuthenticatedPrincipal(request);

    assertEquals(1, principal.getIdpEntities().size());
    assertEquals(1, principal.getSpEntities().size());
    assertEquals(2, principal.getAuthorities().size());
    assertEquals("[ROLE_ADMIN, ROLE_USER]", principal.getAuthorities().toString());
    assertEquals("John Doe", principal.getDisplayName());
    assertEquals("urn:collab:person:example.com:admin", principal.getUsername());

  }
}