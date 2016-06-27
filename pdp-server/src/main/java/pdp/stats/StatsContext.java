package pdp.stats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StatsContext {

  private String serviceProvicer;
  private String identityProvider;
  private long responseTimeMs;
  private Map<String, Long> pipResponses = new HashMap<>();
  private String decision;

  public String getServiceProvicer() {
    return serviceProvicer;
  }

  public void setServiceProvicer(String serviceProvicer) {
    this.serviceProvicer = serviceProvicer;
  }

  public String getIdentityProvider() {
    return identityProvider;
  }

  public void setIdentityProvider(String identityProvider) {
    this.identityProvider = identityProvider;
  }

  public long getResponseTimeMs() {
    return responseTimeMs;
  }

  public void setResponseTimeMs(long responseTimeMs) {
    this.responseTimeMs = responseTimeMs;
  }

  public String getDecision() {
    return decision;
  }

  public void setDecision(String decision) {
    this.decision = decision;
  }

  public Map<String, Long> getPipResponses() {
    return pipResponses;
  }

  public void addPipResponse(String name, long ms) {
    this.pipResponses.put(name, ms);
  }
}
