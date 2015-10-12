package pdp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "pdp_policy_violations")
public class PdpPolicyViolation {
  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false)
  private String policyId;

  @Column(nullable = false)
  private String policyName;

  @Column(nullable = false)
  private String jsonRequest;

  @Column(nullable = false)
  private String response;

  @Column()
  private Timestamp created;

  public PdpPolicyViolation() {
  }

  public PdpPolicyViolation(String policyId, String policyName, String jsonRequest, String response) {
    this.policyId = policyId;
    this.policyName = policyName;
    this.jsonRequest = jsonRequest;
    this.response = response;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public String getPolicyName() {
    return policyName;
  }

  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  public String getJsonRequest() {
    return jsonRequest;
  }

  public void setJsonRequest(String jsonRequest) {
    this.jsonRequest = jsonRequest;
  }

  public Timestamp getCreated() {
    return created;
  }

  public void setCreated(Timestamp created) {
    this.created = created;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

}
