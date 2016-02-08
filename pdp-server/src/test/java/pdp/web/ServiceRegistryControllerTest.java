package pdp.web;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import pdp.access.PolicyIdpAccessAwareToken;
import pdp.access.RunAsFederatedUser;
import pdp.domain.EntityMetaData;
import pdp.serviceregistry.ClassPathResourceServiceRegistry;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ServiceRegistryControllerTest {

  private ServiceRegistryController subject = new ServiceRegistryController(new ClassPathResourceServiceRegistry(true));

  @Test
  public void testServiceProviders() throws Exception {
    assertEquals(24, subject.serviceProviders().size());
  }

  @Test
  public void testIdentityProviders() throws Exception {
    assertEquals(8, subject.identityProviders().size());
  }

  @Test
  public void testIdentityProvidersScoped() throws Exception {
    setupSecurityContext(Collections.emptySet(), Collections.emptySet());
    assertEquals(0, subject.identityProvidersScoped().size());

  }

  private void setupSecurityContext(Set<EntityMetaData> idpEntities, Set<EntityMetaData> spEntities) {
    SecurityContext context = new SecurityContextImpl();
    Authentication authentication = new PolicyIdpAccessAwareToken(
        new RunAsFederatedUser("uid", "unknown-idp", "John Doe", idpEntities, spEntities, Collections.emptyList()));
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}