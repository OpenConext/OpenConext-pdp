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
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class SabClient {

    private final static Logger LOG = LoggerFactory.getLogger(SabClient.class);

    private final String sabRestEndpoint;

    private final RestTemplate restTemplate;

    public SabClient(String sabUserName, String sabPassword, String sabEndpoint) {
        if (!sabEndpoint.endsWith("/")) {
            sabEndpoint += "/";
        }
        sabEndpoint += "api/profile?uid={uid}&idp={idp}";
        this.sabRestEndpoint = sabEndpoint;

        this.restTemplate = new RestTemplate();
        this.restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(
                sabUserName, sabPassword
        ));
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(String userId) {
        LOG.debug("Starting to fetch SAB roles for {} from {}", userId, sabRestEndpoint);
        String[] splitted = userId.split(":");
        if (splitted.length < 3) {
            throw new IllegalArgumentException(String.format("Illegal userId. Not a valid unspecified %s", userId));
        }
        String uid = splitted[splitted.length -1 ];
        String schacHome = splitted[splitted.length -2 ];

        Map result = this.restTemplate.getForObject(sabRestEndpoint, Map.class, uid, schacHome);
        if (result == null || !result.containsKey("message") || !result.get("message").equals("OK")) {
            LOG.warn("Error from SAB roles for {} from {}. Returning empty List", userId, sabRestEndpoint);
            return emptyList();
        }
        List<Map<String, Object>> profiles = (List<Map<String, Object>>) result.get("profiles");
        if (CollectionUtils.isEmpty(profiles)) {
            return emptyList();
        }
        Map<String, Object> profile = profiles.get(0);
        return getAuthorisationEntitlements(profile);
    }

    private List<String> getAuthorisationEntitlements(Map<String, Object> profile) {
        if (profile.containsKey("authorisations")) {
            List<Map<String, String>> authorisations = (List<Map<String, String>>) profile.get("authorisations");
            return authorisations.stream()
                    .map(m -> "urn:mace:surfnet.nl:surfnet.nl:sab:role:" + m.get("role"))
                    .collect(toList());
        }
        return new ArrayList<>();
    }

}
