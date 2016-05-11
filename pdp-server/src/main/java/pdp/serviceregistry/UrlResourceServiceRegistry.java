package pdp.serviceregistry;

import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class UrlResourceServiceRegistry extends ClassPathResourceServiceRegistry {

  private final String idpRemotePath;
  private final String spRemotePath;

  private final RestTemplate restTemplate = new RestTemplate();
  private final int period;
  private final BasicAuthenticationUrlResource idpUrlResource;
  private final BasicAuthenticationUrlResource spUrlResource;
  private ScheduledFuture<?> scheduledFuture;
  private boolean lastCallFailed = true;

  public UrlResourceServiceRegistry(
      String username,
      String password,
      String idpRemotePath,
      String spRemotePath,
      int period) throws MalformedURLException {
    super(false);

    this.idpUrlResource = new BasicAuthenticationUrlResource(idpRemotePath, username, password);
    this.spUrlResource = new BasicAuthenticationUrlResource(spRemotePath, username, password);

    this.idpRemotePath = idpRemotePath;
    this.spRemotePath = spRemotePath;
    this.period = period;

    SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    requestFactory.setConnectTimeout(5 * 1000);

    schedule(period, TimeUnit.MINUTES);
    doInitializeMetadata(true);
  }

  private void schedule(int period, TimeUnit timeUnit) {
    if (this.scheduledFuture != null) {
      this.scheduledFuture.cancel(true);
    }
    this.scheduledFuture = newScheduledThreadPool(1).scheduleAtFixedRate(this::initializeMetadata, period, period, timeUnit);
  }

  @Override
  protected Resource getIdpResource() {
    LOG.debug("Fetching IDP metadata entries from {}", idpRemotePath);
    return idpUrlResource;
  }

  @Override
  protected Resource getSpResource() {
    LOG.debug("Fetching SP metadata entries from {}", spRemotePath);
    return spUrlResource;
  }

  private void doInitializeMetadata(boolean forceRefresh) {
    try {
      if (forceRefresh ||  spUrlResource.isModified(period) || idpUrlResource.isModified(period) ) {
        super.initializeMetadata();
      } else {
        LOG.debug("Not refreshing SP metadata. Not modified");
      }
      //now maybe this is the first successful call after a failure, so check and change the period
      if (lastCallFailed) {
        schedule(period, TimeUnit.MINUTES);
      }
      lastCallFailed = false;
    } catch (Throwable e) {
      /*
       * By design we catch the error and not rethrow it.
       *
       * UrlResourceServiceRegistry has timing issues when the server reboots and required MetadataExporter endpoints
       * are not available yet. We re-schedule the timer to try every 5 seconds until it's succeeds
       */
      LOG.error("Error in refreshing / initializing metadata", e);
      lastCallFailed = true;
      schedule(5, TimeUnit.SECONDS);
    }
  }

  @Override
  protected void initializeMetadata() {
    doInitializeMetadata(false);
  }


}
