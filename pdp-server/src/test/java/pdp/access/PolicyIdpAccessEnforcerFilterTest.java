package pdp.access;


import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.context.SecurityContextHolder;
import pdp.policies.PolicyLoader;
import pdp.serviceregistry.ClassPathResourceServiceRegistry;

import javax.servlet.FilterChain;

import java.util.Arrays;

import static java.util.Base64.getEncoder;
import static org.junit.Assert.assertEquals;
import static pdp.access.FederatedUserBuilder.*;

public class PolicyIdpAccessEnforcerFilterTest {

  private PolicyIdpAccessEnforcerFilter subject;

  @Before
  public void before() throws Exception {
    ProviderManager providerManager = new ProviderManager(Arrays.asList(new BasicAuthenticationProvider("user", "password")));
    subject = new PolicyIdpAccessEnforcerFilter(providerManager, new ClassPathResourceServiceRegistry(true));
    SecurityContextHolder.clearContext();
  }

  @Test
  public void testOnSuccessfulAuthentication() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain();

    request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoder().encodeToString(new String("user:password").getBytes()));

    request.addHeader(X_IDP_ENTITY_ID, PolicyLoader.authenticatingAuthority);
    request.addHeader(X_UNSPECIFIED_NAME_ID, "uid");
    request.addHeader(X_DISPLAY_NAME, "John Doe");

    subject.doFilter(request, response, filterChain);

    RunAsFederatedUser user = (RunAsFederatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    assertEquals("uid", user.getIdentifier());
    assertEquals("John Doe", user.getDisplayName());
    assertEquals(2, user.getIdpEntities().size());
    assertEquals(3, user.getSpEntities().size());
    assertEquals(PolicyLoader.authenticatingAuthority, user.getAuthenticatingAuthority());
    assertEquals("[ROLE_PEP, ROLE_USER]", user.getAuthorities().toString());


  }
}