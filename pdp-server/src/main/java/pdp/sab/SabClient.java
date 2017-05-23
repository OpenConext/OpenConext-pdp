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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class SabClient {

    private final static Logger LOG = LoggerFactory.getLogger(SabClient.class);

    private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

    private final String sabUserName;
    private final String sabPassword;
    private final String sabEndpoint;

    private final RestTemplate restTemplate;
    private final String template;

    private final SabResponseParser parser = new SabResponseParser();

    private final int timeOut = 1000 * 10;

    public SabClient(String sabUserName, String sabPassword, String sabEndpoint) {
        this.sabUserName = sabUserName;
        this.sabPassword = sabPassword;
        this.sabEndpoint = sabEndpoint;

        try {
            this.template = IOUtils.toString(new ClassPathResource("sab/request.xml").getInputStream());
            this.restTemplate = new RestTemplate(asList(new ByteArrayHttpMessageConverter(), new StringHttpMessageConverter()));
            this.restTemplate.setRequestFactory(getRequestFactory());
        } catch (IOException e) {
            //fail fast
            throw new RuntimeException(e);
        }
    }

    public List<String> roles(String userId) throws IOException {
        String request = request(userId);
        ResponseEntity<byte[]> response = restTemplate.exchange(sabEndpoint, HttpMethod.POST, new HttpEntity<>(request), byte[].class);
        try {
            List<String> roles = parser.parse(new ByteArrayInputStream(response.getBody()));
            LOG.debug("Retrieved SAB roles with request: {} and response: {}", request, response);
            return roles;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String request(String userId) throws IOException {
        String issueInstant = dateTimeFormatter.print(System.currentTimeMillis());
        return MessageFormat.format(template, UUID.randomUUID().toString(), issueInstant, userId);
    }

    //lot of code to prevent preemptive authentication
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
