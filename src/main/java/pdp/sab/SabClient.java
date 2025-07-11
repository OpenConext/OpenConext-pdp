package pdp.sab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

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
        String[] splitted = userId.split(":");
        if (splitted.length < 3) {
            throw new IllegalArgumentException(String.format("Illegal userId. Not a valid unspecified %s", userId));
        }
        String uid = splitted[splitted.length - 1];
        String idp = splitted[splitted.length - 2];

        LOG.debug("Starting to fetch SAB roles for userId {}, schacHome {}, uid {} using templateURI {}",
                userId, idp, userId, sabRestEndpoint);
        Map result;
        try {
            result = this.restTemplate.getForObject(sabRestEndpoint, Map.class, uid, idp);
        } catch (RestClientException e) {
            LOG.error("Error from restTemplate in SabClient", e);
            throw e;
        }

        if (result == null || !result.containsKey("message") || !result.get("message").equals("OK")) {
            LOG.warn("Error from SAB roles for {} from {}. Returning empty List", userId, sabRestEndpoint);
            return emptyList();
        }
        List<Map<String, Object>> profiles = (List<Map<String, Object>>) result.get("profiles");
        if (CollectionUtils.isEmpty(profiles)) {
            LOG.debug("Empty result from SAB roles for {} from {}.", userId, sabRestEndpoint);
            return emptyList();
        }
        Map<String, Object> profile = profiles.getFirst();
        List<String> sabRoles = getAuthorisationEntitlements(profile);

        LOG.debug("Result {} from SAB roles for {} from {}.", sabRoles, userId, sabRestEndpoint);

        return sabRoles;
    }

    private List<String> getAuthorisationEntitlements(Map<String, Object> profile) {
        if (profile.containsKey("authorisations")) {
            List<Map<String, String>> authorisations = (List<Map<String, String>>) profile.get("authorisations");
            return authorisations.stream()
                    .map(m -> m.get("role"))
                    .toList();
        }
        return new ArrayList<>();
    }

}
