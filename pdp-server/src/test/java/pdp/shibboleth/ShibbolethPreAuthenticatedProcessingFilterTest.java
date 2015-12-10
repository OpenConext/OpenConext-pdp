package pdp.shibboleth;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import pdp.access.FederatedUser;
import pdp.serviceregistry.ClassPathResourceServiceRegistry;

import static org.junit.Assert.assertEquals;
import static pdp.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.*;

public class ShibbolethPreAuthenticatedProcessingFilterTest {

  private final static ShibbolethPreAuthenticatedProcessingFilter filter = new ShibbolethPreAuthenticatedProcessingFilter(null, new ClassPathResourceServiceRegistry("test"));

  @Test
  public void testGetPreAuthenticatedPrincipal() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    request.addHeader(UID_HEADER_NAME, "urn:collab:person:example.com:admin");
    request.addHeader(SHIB_AUTHENTICATING_AUTHORITY, "http://adfs2prod.aventus.nl/adfs/services/trust");
    request.addHeader(DISPLAY_NAME_HEADER_NAME, "John Doe");
    request.addHeader(IS_MEMBER_OF, "surfnet");

    FederatedUser principal = (FederatedUser) filter.getPreAuthenticatedPrincipal(request);

    assertEquals(4, principal.getIdpEntities().size());
    assertEquals(2, principal.getSpEntities().size());
    assertEquals(2, principal.getAuthorities().size());
    assertEquals("[ROLE_ADMIN, ROLE_USER]", principal.getAuthorities().toString());
    assertEquals("John Doe", principal.getDisplayName());
    assertEquals("urn:collab:person:example.com:admin", principal.getUsername());

  }
}