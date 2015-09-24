package pdp;

import org.junit.Test;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
    assertEquals(293,idps.size());
    assertTrue(idps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
  }
}