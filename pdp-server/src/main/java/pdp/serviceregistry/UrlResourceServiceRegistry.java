package pdp.serviceregistry;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;

public class UrlResourceServiceRegistry extends ClassPathResourceServiceRegistry {

  private final String idpRemotePath;
  private final String spRemotePath;

  private final RestTemplate restTemplate = new RestTemplate();
  private final int period;

  public UrlResourceServiceRegistry(
      String idpRemotePath,
      String spRemotePath,
      int period) {
    super("prod", false);
    this.idpRemotePath = idpRemotePath;
    this.spRemotePath = spRemotePath;
    this.period = period;
    newScheduledThreadPool(1).scheduleAtFixedRate(() ->
        this.initializeMetadata(), period, period, TimeUnit.MINUTES);
    this.initializeMetadata();
  }

  @Override
  protected List<Resource> getIdpResources() {
    LOG.debug("Fetching IDP metadata entries from {}", idpRemotePath);
    return Arrays.asList(getResource(idpRemotePath));
  }

  @Override
  protected List<Resource> getSpResources() {
    LOG.debug("Fetching SP metadata entries from {}", spRemotePath);
    return Arrays.asList(getResource(spRemotePath));
  }

  @Override
  protected void initializeMetadata() {
    HttpHeaders headers = new HttpHeaders();
    String oneMinuteAgo = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")).minusMinutes(1));
    headers.add(IF_MODIFIED_SINCE, oneMinuteAgo);

    ResponseEntity<String> result = restTemplate.exchange(idpRemotePath, HttpMethod.HEAD, new HttpEntity<>(headers), String.class);

    if (!result.getStatusCode().equals(NOT_MODIFIED)) {
      super.initializeMetadata();
    }
  }

  private Resource getResource(String path) {
    try {
      return new UrlResource(path);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
