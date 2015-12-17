package pdp.serviceregistry;

import org.springframework.core.io.Resource;
import org.springframework.data.util.ReflectionUtils;
import pdp.domain.EntityMetaData;

import java.util.List;

public class TestingServiceRegistry extends ClassPathResourceServiceRegistry {

  private final String spResource;
  private final String idpResource;

  public TestingServiceRegistry() {
    this(null, null);
  }

  public TestingServiceRegistry(String idpResource, String spResource) {
    super(false);
    this.idpResource = idpResource;
    this.spResource = spResource;
    initializeMetadata();
  }

  @Override
  protected List<Resource> getIdpResources() {
    return idpResource != null ? doGetResources(idpResource) : doGetResources("service-registry/saml20-idp-remote.test.json");
  }

  @Override
  protected List<Resource> getSpResources() {
    return spResource != null ? doGetResources(spResource) : doGetResources("service-registry/saml20-sp-remote.test.json");
  }

  public void allowAll(boolean allowAll) {
    identityProviders().forEach(md -> doAllowAll(md, allowAll));
    serviceProviders().forEach(md -> doAllowAll(md, allowAll));
  }

  private void doAllowAll(EntityMetaData md, boolean allowAll) {
    try {
      ReflectionUtils.setField(EntityMetaData.class.getDeclaredField("allowedAll"), md, allowAll);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }
}
