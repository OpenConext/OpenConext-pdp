package pdp.stats;

public class StatsContext {

  private String serviceProvicer;

  public void serviceProvider(String entityId) {
    this.serviceProvicer = entityId;
  }

  public void identityProvider(String entityId) {
    this.serviceProvicer = entityId;
  }

  public void addPIPCall() {

  }

}
