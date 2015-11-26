package pdp.domain;

import pdp.PolicyTemplateEngine;

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
  private String policyId;

  @Column(nullable = false)
  private String policyXml;

  @Column(nullable = false)
  private String name;

  @Column(nullable = true)
  private String authenticatingAuthority;

  @Column(nullable = true)
  private String userIdentifier;

  public PdpPolicy() {
  }

  public PdpPolicy(String policyXml, String name, String userIdentifier, String authenticatingAuthority) {
    this.policyXml = policyXml;
    this.name = name;
    this.userIdentifier = userIdentifier;
    this.authenticatingAuthority = authenticatingAuthority;
    this.policyId = PolicyTemplateEngine.getPolicyId(name);
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

  public String getAuthenticatingAuthority() {
    return authenticatingAuthority;
  }

  public void setAuthenticatingAuthority(String authenticatingAuthority) {
    this.authenticatingAuthority = authenticatingAuthority;
  }

  public String getUserIdentifier() {
    return userIdentifier;
  }

  public void setUserIdentifier(String userIdentifier) {
    this.userIdentifier = userIdentifier;
  }
}
