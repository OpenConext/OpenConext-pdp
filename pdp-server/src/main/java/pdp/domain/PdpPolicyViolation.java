package pdp.domain;

import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.sql.Date;

@Entity(name = "pdp_policy_violations")
public class PdpPolicyViolation {
  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false)
  private String policyId;

  @Column(nullable = false)
  private String jsonRequest;

  @Column(nullable = false)
  private String response;

  @Column()
  private Date created;

  public PdpPolicyViolation() {
  }

  public PdpPolicyViolation(String policyId, String jsonRequest, String response) {
    this.policyId = policyId;
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

  public String getJsonRequest() {
    return jsonRequest;
  }

  public void setJsonRequest(String jsonRequest) {
    this.jsonRequest = jsonRequest;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  @Transient
  public boolean isValid() {
    return StringUtils.hasText(policyId)
        && StringUtils.hasText(jsonRequest)
        && StringUtils.hasText(response);
  }
}
