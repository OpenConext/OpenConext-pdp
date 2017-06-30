package pdp.teams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class VootClient {

    private final static Logger LOG = LoggerFactory.getLogger(VootClient.class);

    private final String vootServiceUrl;

    private final RestOperations vootService;

    public VootClient(RestOperations vootService, String vootServiceUrl) {
        this.vootService = vootService;
        this.vootServiceUrl = vootServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public List<String> groups(String userUrn) {
        long start = System.currentTimeMillis();
        LOG.info("Starting to retrieve groups for {}", userUrn);
        List<Map<String, Object>> groups = (List<Map<String, Object>>) vootService.getForObject(vootServiceUrl + "/internal/groups/{userUrn}", List.class, userUrn);
        LOG.debug("Retrieved groups: {}", groups);
        LOG.info("finished retrieving groups for {} in {} ms", userUrn, System.currentTimeMillis() - start);
        return groups.stream().map(entry -> (String) entry.get("id")).collect(toList());
    }
}
