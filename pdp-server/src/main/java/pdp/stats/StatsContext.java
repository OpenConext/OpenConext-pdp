package pdp.stats;

import lombok.Getter;
import lombok.Setter;
import org.apache.openaz.xacml.api.IdReference;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class StatsContext {

    private String serviceProvicer;
    private String identityProvider;
    private long responseTimeMs;
    private Map<String, Long> pipResponses = new HashMap<>();
    private String decision;
    private String loa;
    private String policyId;

    public void addPipResponse(String name, long ms) {
        this.pipResponses.put(name, ms);
    }

}
