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

  private final String idpRemotePath;
  private final String spRemotePath;
  private final String userName;
  private final String password;

  public UrlResourceServiceRegistry(
      String idpRemotePath,
      String spRemotePath,
      String userName,
      String password,
      int initialDelay,
      int period) {
    super("prod", false);
    this.idpRemotePath = idpRemotePath;
    this.spRemotePath = spRemotePath;
    this.userName = userName;
    this.password = password;
    newScheduledThreadPool(1).scheduleAtFixedRate(() ->
        this.initializeMetadata(), initialDelay, period, TimeUnit.MINUTES);
    this.initializeMetadata();
  }

  @Override
  protected List<Resource> getIdpResources() {
    LOG.debug("Fetching IDP metadata entries from {}",idpRemotePath);
    return Arrays.asList(getResource(idpRemotePath));
  }

  @Override
  protected List<Resource> getSpResources() {
    LOG.debug("Fetching SP metadata entries from {}",spRemotePath);
    return Arrays.asList(getResource(spRemotePath));
  }

  private Resource getResource(String path) {
    try {
      return new AuthorizationURLResource(path, userName, password);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
