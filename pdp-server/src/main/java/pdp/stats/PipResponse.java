package pdp.stats;

public class PipResponse {

  private String name;
  private long responseTime;

  public PipResponse() {
  }

  public PipResponse(String name, long responseTime) {
    this.name = name;
    this.responseTime = responseTime;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getResponseTime() {
    return responseTime;
  }

  public void setResponseTime(long responseTime) {
    this.responseTime = responseTime;
  }
}
