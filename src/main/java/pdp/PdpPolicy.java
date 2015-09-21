package pdp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "pdp_policies")
public class PdpPolicy {

  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false)
  private String policyXml;

  public PdpPolicy() {
  }

  public PdpPolicy(String policyXml) {
    this(null, policyXml);
  }

  public PdpPolicy(Long id, String policyXml) {
    this.id = id;
    this.policyXml = policyXml;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPolicyXml() {
    return policyXml;
  }

  public void setPolicyXml(String policyXml) {
    this.policyXml = policyXml;
  }

}
