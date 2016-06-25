package pdp.stats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class StatsContext {

  private String serviceProvicer;
  private String identityProvider;
  private LocalDateTime when;
  private long responseTimeMs;
  private List<PipResponse> pipResponses ;
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

  public List<PipResponse> getPipResponses() {
    return pipResponses;
  }

  public void setPipResponses(List<PipResponse> pipResponses) {
    this.pipResponses = pipResponses;
  }

  public String getDecision() {
    return decision;
  }

  public void setDecision(String decision) {
    this.decision = decision;
  }

  public LocalDateTime getWhen() {
    return when;
  }

  public void setWhen(LocalDateTime when) {
    this.when = when;
  }

  public void addPipResponse(PipResponse pipResponse) {
    if (this.pipResponses == null) {
      this.pipResponses = new ArrayList<>();
    }
    this.pipResponses.add(pipResponse);
  }
}
