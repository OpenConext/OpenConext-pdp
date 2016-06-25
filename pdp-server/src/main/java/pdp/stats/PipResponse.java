package pdp.stats;

public class PipResponse {

  private String name;
  private long responseTime;

  public PipResponse(String name, long responseTime) {
    this.name = name;
    this.responseTime = responseTime;
  }

  public String getName() {
    return name;
  }

  public long getResponseTime() {
    return responseTime;
  }
}
