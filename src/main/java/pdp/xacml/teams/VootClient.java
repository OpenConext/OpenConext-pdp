package pdp.xacml.teams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VootClient {

  private final static Logger LOG = LoggerFactory.getLogger(VootClient.class);

  private final String vootServiceUrl;

  private final RestOperations vootService;

  public VootClient(RestOperations vootService, String vootServiceUrl) {
    this.vootService = vootService;
    this.vootServiceUrl = vootServiceUrl;
  }

  public List<String> groups(String userUrn) {
    List<Map<String, Object>> groups = vootService.getForObject(vootServiceUrl + "/internal/groups/{userUrn}", List.class, userUrn);
    LOG.debug("Retrieved groups: {}", groups);
    return Collections.EMPTY_LIST;
  }
}
