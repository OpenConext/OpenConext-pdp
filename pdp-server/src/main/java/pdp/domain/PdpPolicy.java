package pdp.domain;

import org.hibernate.annotations.Formula;
import pdp.PolicyTemplateEngine;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.hibernate.annotations.CascadeType.*;

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

  @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "latest_revision", referencedColumnName = "id")
  private Set<PdpPolicy> revisions = new HashSet<>();

  @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "policy")
  private Set<PdpPolicyViolation> violations = new HashSet<>();

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

  public Set<PdpPolicy> getRevisions() {
    return revisions;
  }

  public void setRevisions(Set<PdpPolicy> revisions) {
    this.revisions = revisions;
  }

  public void addRevision(PdpPolicy revision) {
    this.revisions.add(revision);
  }

  public Set<PdpPolicyViolation> getViolations() {
    return violations;
  }

  public void setViolations(Set<PdpPolicyViolation> violations) {
    this.violations = violations;
  }

  public void addPdpPolicyViolation(PdpPolicyViolation violation) {
    this.violations.add(violation);
  }

  public PdpPolicy clone() {
    return new PdpPolicy(policyXml, name, userIdentifier, authenticatingAuthority);
  }
}
