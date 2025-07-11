package pdp.teams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class VootClient {

    private final static Logger LOG = LoggerFactory.getLogger(VootClient.class);

    private final String vootServiceUrl;

    private final WebClient vootService;

    public VootClient(WebClient vootService, String vootServiceUrl) {
        this.vootService = vootService;
        this.vootServiceUrl = vootServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public List<String> groups(String userUrn) {
        long start = System.currentTimeMillis();
        LOG.info("Starting to retrieve groups for {}", userUrn);

        String uri = vootServiceUrl + "/internal/groups/" + userUrn;
        List<Map<String, Object>> groups = (List<Map<String, Object>>) this.vootService.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(List.class)
                .block();
        LOG.debug("Retrieved groups: {}", groups);
        LOG.info("finished retrieving groups for {} in {} ms", userUrn, System.currentTimeMillis() - start);

        return groups.stream().map(entry -> (String) entry.get("id")).collect(toList());
    }
}
