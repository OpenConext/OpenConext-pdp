package pdp.serviceregistry;

import org.springframework.core.io.Resource;

import java.util.List;

public class TestingServiceRegistry extends ClassPathResourceServiceRegistry {

  public TestingServiceRegistry() {
    super(true);
  }

  @Override
  protected List<Resource> getIdpResources() {
    return doGetResources("service-registry/saml20-idp-remote.test.json");
  }

  @Override
  protected List<Resource> getSpResources() {
    return doGetResources("service-registry/saml20-sp-remote.test.json");
  }
}
