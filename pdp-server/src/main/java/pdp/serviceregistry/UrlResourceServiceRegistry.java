package pdp.serviceregistry;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class UrlResourceServiceRegistry extends ClassPathResourceServiceRegistry implements HealthIndicator {

  private final static ZoneId GMT = ZoneId.of("GMT");

  private final String idpRemotePath;
  private final String spRemotePath;

  private final RestTemplate restTemplate = new RestTemplate();
  private final int refreshInMinutes;
  private final BasicAuthenticationUrlResource idpUrlResource;
  private final BasicAuthenticationUrlResource spUrlResource;

  private ScheduledFuture<?> scheduledFuture;
  private boolean lastCallFailed = true;

  private volatile ZonedDateTime lastRefreshCheck = ZonedDateTime.now(GMT);
  private volatile ZonedDateTime metadataLastUpdated = ZonedDateTime.now(GMT);

  public UrlResourceServiceRegistry(
      String username,
      String password,
      String idpRemotePath,
      String spRemotePath,
      int refreshInMinutes) throws MalformedURLException {
    super(false);

    this.idpUrlResource = new BasicAuthenticationUrlResource(idpRemotePath, username, password);
    this.spUrlResource = new BasicAuthenticationUrlResource(spRemotePath, username, password);

    this.idpRemotePath = idpRemotePath;
    this.spRemotePath = spRemotePath;
    this.refreshInMinutes = refreshInMinutes;

    SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    requestFactory.setConnectTimeout(5 * 1000);

    schedule(this.refreshInMinutes, TimeUnit.MINUTES);
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
      this.lastRefreshCheck = ZonedDateTime.now(GMT);

      if (forceRefresh ||  spUrlResource.isModified(refreshInMinutes) || idpUrlResource.isModified(refreshInMinutes) ) {
        this.metadataLastUpdated = ZonedDateTime.now(GMT);
        super.initializeMetadata();
      } else {
        LOG.debug("Not refreshing SP metadata. Not modified");
      }
      //now maybe this is the first successful call after a failure, so check and change the refreshInMinutes
      if (lastCallFailed) {
        schedule(refreshInMinutes, TimeUnit.MINUTES);
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

  @Override
  public Health health() {
    Health.Builder healthBuilder = lastRefreshCheck.plusMinutes(refreshInMinutes + 2).isBefore(ZonedDateTime.now(GMT)) ? Health.down() : Health.up();

    return healthBuilder
        .withDetail("lastMetaDataRefreshCheck", lastRefreshCheck.format(DateTimeFormatter.ISO_DATE_TIME))
        .withDetail("serviceProvidersLastUpdated", metadataLastUpdated.format(DateTimeFormatter.ISO_DATE_TIME))
        .build();
  }

}
