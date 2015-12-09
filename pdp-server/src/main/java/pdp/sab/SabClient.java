package pdp.sab;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SabClient {

  private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

  private final String sabUserName;
  private final String sabPassword;
  private final String sabEndpoint;

  private final RestTemplate restTemplate;
  private final SabResponseParser parser = new SabResponseParser();

  private final int timeOut = 1000 * 10;

  public SabClient(String sabUserName, String sabPassword, String sabEndpoint) {
    this.sabUserName = sabUserName;
    this.sabPassword = sabPassword;
    this.sabEndpoint = sabEndpoint;

    try {
      this.restTemplate = new RestTemplate(getRequestFactory());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> roles(String userId) throws IOException {
    String request = request(userId);
    String response = restTemplate.postForEntity(sabEndpoint, request, String.class).getBody();
    try {
      return parser.parse(response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  private String request(String userId) throws IOException {
    String template = IOUtils.toString(new ClassPathResource("sab/request.xml").getInputStream());
    String issueInstant = dateTimeFormatter.print(System.currentTimeMillis());
    return MessageFormat.format(template, UUID.randomUUID().toString(), issueInstant, userId);
  }

  private ClientHttpRequestFactory getRequestFactory() throws MalformedURLException {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().evictExpiredConnections().evictIdleConnections(10l, TimeUnit.SECONDS);
    BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
    basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(sabUserName, sabPassword));
    httpClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider);
    httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(timeOut).setConnectTimeout(timeOut).setSocketTimeout(timeOut).build());

    CloseableHttpClient httpClient = httpClientBuilder.build();
    return new PreemptiveAuthenticationHttpComponentsClientHttpRequestFactory(httpClient, sabEndpoint);
  }

}
