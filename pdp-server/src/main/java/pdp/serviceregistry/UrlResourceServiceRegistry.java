package pdp.serviceregistry;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class UrlResourceServiceRegistry extends ClassPathResourceServiceRegistry {

  public UrlResourceServiceRegistry(int initialDelay, int period) {
    super("prod");
    newScheduledThreadPool(1).scheduleAtFixedRate(() ->
        this.initializeMetadata(), initialDelay, period, TimeUnit.MINUTES);
  }

  @Override
  protected List<Resource> getIdpResources() {
    return Arrays.asList(getResource("https://tools.surfconext.nl/export/saml20-idp-remote.json"));
  }

  @Override
  protected List<Resource> getSpResources() {
    return Arrays.asList(getResource("https://tools.surfconext.nl/export/saml20-sp-remote.json"));
  }

  private Resource getResource(String path) {
    try {
      return new UrlResource(path);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
