package pdp.serviceregistry;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UrlResourceServiceRegistry extends ClassPathResourceServiceRegistry {

  public UrlResourceServiceRegistry(int initialDelay, int period) {
    super();

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable task = () -> this.initializeMetadata();
    executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MINUTES);
  }

  @Override
  protected Resource getIdpResource() {
    return getResource("https://tools.surfconext.nl/export/saml20-idp-remote.json");
  }

  @Override
  protected Resource getSpResource() {
    return getResource("https://tools.surfconext.nl/export/saml20-sp-remote.json");
  }

  private Resource getResource(String path) {
    try {
      return new UrlResource(path);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
