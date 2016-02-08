package pdp.shibboleth;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import pdp.access.FederatedUser;
import pdp.access.RunAsFederatedUser;
import pdp.policies.PolicyLoader;
import pdp.serviceregistry.ClassPathResourceServiceRegistry;

import static java.util.Collections.emptySet;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static pdp.access.FederatedUserBuilder.*;

public class ShibbolethPreAuthenticatedProcessingFilterTest {

  private final static ShibbolethPreAuthenticatedProcessingFilter filter = new ShibbolethPreAuthenticatedProcessingFilter(null, new ClassPathResourceServiceRegistry(true));

  @Test
  public void testGetPreAuthenticatedShibPrincipal() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    addShibHeaders(request);

    FederatedUser principal = (FederatedUser) filter.getPreAuthenticatedPrincipal(request);

    assertPrincipal(principal, "[ROLE_ADMIN, ROLE_USER]");
    assertFalse(principal.isPolicyIdpAccessEnforcementRequired());

  }

  @Test
  public void testGetPreAuthenticatedTrustedApiPrincipal() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    addShibHeaders(request);
    //trusted API headers
    request.addHeader(X_UNSPECIFIED_NAME_ID, "urn:collab:person:example.com:admin");
    request.addHeader(X_IDP_ENTITY_ID, PolicyLoader.authenticatingAuthority);
    request.addHeader(X_DISPLAY_NAME, "John Doe");
    request.addHeader(X_IMPERSONATE, true);

    RunAsFederatedUser principal = (RunAsFederatedUser) filter.getPreAuthenticatedPrincipal(request);

    assertPrincipal(principal, "[ROLE_PEP, ROLE_USER]");
    assertTrue(principal.isPolicyIdpAccessEnforcementRequired());
  }

  @Test
  public void testEmptyGetPreAuthenticatedPrincipal() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    FederatedUser principal = (FederatedUser) filter.getPreAuthenticatedPrincipal(request);

    assertNull(principal);
  }

  @Test
  public void principalChanged() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    assertFalse(filter.principalChanged(request,
        new TestingAuthenticationToken(new FederatedUser(
            "uid", "mock-idp", "John Doe", emptySet(), emptySet(),
            AuthorityUtils.createAuthorityList("USER")), "N/A")
        )
    );

    assertTrue(filter.principalChanged(request,
        new TestingAuthenticationToken(new RunAsFederatedUser(
            "uid", "mock-idp", "John Doe", emptySet(), emptySet(),
            AuthorityUtils.createAuthorityList("USER")), "N/A")
        )
    );

    request.addHeader(X_IMPERSONATE, true);
    assertTrue(filter.principalChanged(request, null));
  }

  private void addShibHeaders(MockHttpServletRequest request) {
    request.addHeader(UID_HEADER_NAME, "urn:collab:person:example.com:admin");
    request.addHeader(SHIB_AUTHENTICATING_AUTHORITY, PolicyLoader.authenticatingAuthority);
    request.addHeader(DISPLAY_NAME_HEADER_NAME, "John Doe");
  }

  private void assertPrincipal(FederatedUser principal, String authorities) {
    assertEquals(2, principal.getIdpEntities().size());
    assertEquals(3, principal.getSpEntities().size());
    assertEquals(2, principal.getAuthorities().size());
    assertEquals(authorities, principal.getAuthorities().toString());
    assertEquals("John Doe", principal.getDisplayName());
    assertEquals("urn:collab:person:example.com:admin", principal.getUsername());
  }

}