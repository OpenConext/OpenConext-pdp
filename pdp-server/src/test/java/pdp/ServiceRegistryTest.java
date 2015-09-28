package pdp;

import org.junit.Test;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceRegistryTest {

  private ServiceRegistry serviceRegistry = new ServiceRegistry();

  @Test
  public void testServiceProviders() throws Exception {
    List<EntityMetaData> sps = serviceRegistry.serviceProviders("en");
    assertEquals(943, sps.size());
    assertTrue(sps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
  }

  @Test
  public void testIdentityProviders() throws Exception {
    List<EntityMetaData> idps = serviceRegistry.identityProviders("nl");
    assertEquals(293, idps.size());
    assertTrue(idps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
  }

  @Test
  public void testIdentityProvidersLanguageDefault() throws Exception {
    List<EntityMetaData> idps = serviceRegistry.identityProviders("xxx");
    assertEquals(293, idps.size());
    assertTrue(idps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
  }
}