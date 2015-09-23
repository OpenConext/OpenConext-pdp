package pdp.domain;

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

  @Column(nullable = false)
  private String name;

  public PdpPolicy() {
  }

  public PdpPolicy(String policyXml, String name) {
    this.policyXml = policyXml;
    this.name = name;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
