package pdp.serviceregistry;

import org.junit.Test;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pdp.PdpApplication.singletonOptionalCollector;

public class ClassPathResourceServiceRegistryTest {

  private static ServiceRegistry serviceRegistry = new ClassPathResourceServiceRegistry("test");

  @Test
  public void testServiceProviders() throws Exception {
    List<EntityMetaData> sps = serviceRegistry.serviceProviders();
    assertEquals(953, sps.size());
    assertTrue(sps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
    // we expect a sorted list
    assertEquals(asList("3", "3", "A"), sps.subList(0, 3).stream().map(e -> e.getNameEn().substring(0, 1)).collect(toList()));
  }

  @Test
  public void testIdentityProviders() throws Exception {
    List<EntityMetaData> idps = serviceRegistry.identityProviders();
    assertEquals(296, idps.size());
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

  @Test
  public void testNonExistingEnvironmentSps() {
    ServiceRegistry env = new ClassPathResourceServiceRegistry("dev");
    assertEquals(295, env.identityProviders().size());
  }

}