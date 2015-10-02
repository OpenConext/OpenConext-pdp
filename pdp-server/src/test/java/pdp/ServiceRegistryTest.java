package pdp;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;
import pdp.serviceregistry.ClassPathResourceServiceRegistry;
import pdp.serviceregistry.ServiceRegistry;
import pdp.serviceregistry.UrlResourceServiceRegistry;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pdp.PdpApplication.singletonOptionalCollector;

public class ServiceRegistryTest {

  private static ServiceRegistry serviceRegistry = new ClassPathResourceServiceRegistry();//new UrlResourceServiceRegistry(1,1);

  @Test
  public void testServiceProviders() throws Exception {
    List<EntityMetaData> sps = serviceRegistry.serviceProviders();
    assertEquals(950, sps.size());
    assertTrue(sps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
  }

  @Test
  public void testIdentityProviders() throws Exception {
    List<EntityMetaData> idps = serviceRegistry.identityProviders();
    assertEquals(295, idps.size());
    assertTrue(idps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
  }

  @Test
  public void testInstitutionId() throws Exception {
    Optional<EntityMetaData> saraOptional = serviceRegistry.identityProviders().stream().filter(idp -> idp.getEntityId().equals("http://sso.sara.nl/adfs/services/trust")).collect(singletonOptionalCollector());
    assertTrue(saraOptional.isPresent());
    assertEquals("SARA", saraOptional.get().getInstitutionId());

  }

  @Test
  public void testIdentityProvidersByAuthenticatingAuthority() throws Exception {
    Set<EntityMetaData> idps = serviceRegistry.identityProvidersByAuthenticatingAuthority("http://adfs2prod.aventus.nl/adfs/services/trust");
    assertEquals(4, idps.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIdentityProvidersByAuthenticatingAuthorityNonExistentIdp() throws Exception {
    serviceRegistry.identityProvidersByAuthenticatingAuthority("no");
  }

  @Test
  public void testIdentityProvidersByAuthenticatingAuthoritySingle() throws Exception {
    Set<EntityMetaData> idps = serviceRegistry.identityProvidersByAuthenticatingAuthority("https://tnamidp.rocvantwente.nl/nidp/saml2/metadata");
    assertEquals(1, idps.size());
  }

  @Test
  public void testServiceProvidersByInstitutionId() {
    Set<EntityMetaData> surfnetSps = serviceRegistry.serviceProvidersByInstitutionId("SURFNET");
    assertEquals(60, surfnetSps.size());
  }

  @Test
  public void testServiceProvidersByInstitutionIdEmpty() {
    Set<EntityMetaData> sps = serviceRegistry.serviceProvidersByInstitutionId("NOOP");
    assertEquals(0, sps.size());
  }
}