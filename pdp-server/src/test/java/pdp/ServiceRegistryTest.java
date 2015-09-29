package pdp;

import org.junit.Test;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pdp.PdpApplication.singletonOptionalCollector;

public class ServiceRegistryTest {

  private ServiceRegistry serviceRegistry = new ServiceRegistry();

  @Test
  public void testServiceProviders() throws Exception {
    List<EntityMetaData> sps = serviceRegistry.serviceProviders();
    assertEquals(943, sps.size());
    assertTrue(sps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
  }

  @Test
  public void testIdentityProviders() throws Exception {
    List<EntityMetaData> idps = serviceRegistry.identityProviders();
    assertEquals(293, idps.size());
    assertTrue(idps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
  }

  @Test
  public void testDescriptionName() throws Exception {
    Optional<EntityMetaData> metaDataOptional = serviceRegistry.identityProviders().stream().filter(idp -> idp.getEntityId().equals("https://sso.sron.nl/nidp/saml2/metadata")).collect(singletonOptionalCollector());
    assertTrue(metaDataOptional.isPresent());
    assertEquals("SRON Netherlands Institute for Space Research", metaDataOptional.get().getDescriptionEn());
    assertEquals("SRON Netherlands Institute voor Ruimte Onderzoek", metaDataOptional.get().getDescriptionNl());
    assertEquals("SRON Holland", metaDataOptional.get().getNameEn());
    assertEquals("SRON Netherland", metaDataOptional.get().getNameNl());
  }

}