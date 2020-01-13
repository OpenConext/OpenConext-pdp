package pdp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pdp.xacml.PolicyTemplateEngine;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "pdp_policies")
public class PdpPolicy {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String policyId;

    @Column
    private String policyXml;

    @Column
    private String name;

    @Column
    private String authenticatingAuthority;

    @Column
    private String userIdentifier;

    @Column
    private String userDisplayName;

    @Column
    private int revisionNbr;

    @Column
    private boolean latestRevision;

    @Column(name = "is_active")
    private boolean active;

    @Column
    private String type;

    @ManyToOne
    @JoinColumn(name = "revision_parent_id", nullable = true)
    //to prevent cycles
    @JsonIgnore
    private PdpPolicy parentPolicy;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "parentPolicy")
    private Set<PdpPolicy> revisions = new HashSet<>();

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "policy")
    private Set<PdpPolicyViolation> violations = new HashSet<>();

    @Column(nullable = false, name = "ts")
    private Date created;

    public PdpPolicy() {
    }

    public PdpPolicy(String policyXml, String name, boolean latestRevision, String userIdentifier, String authenticatingAuthority, String userDisplayName, boolean active, String type) {
        this.policyXml = policyXml;
        this.name = name;
        this.latestRevision = latestRevision;
        this.userIdentifier = userIdentifier;
        this.userDisplayName = userDisplayName;
        this.authenticatingAuthority = authenticatingAuthority;
        this.active = active;
        this.policyId = PolicyTemplateEngine.getPolicyId(name);
        this.type = type;
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

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public Set<PdpPolicy> getRevisions() {
        return revisions;
    }

    public void setRevisions(Set<PdpPolicy> revisions) {
        this.revisions = revisions;
    }

    public void addRevision(PdpPolicy revision) {
        revision.setParentPolicy(this);
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getRevisionNbr() {
        return revisionNbr;
    }

    public void setRevisionNbr(int revisionNbr) {
        this.revisionNbr = revisionNbr;
    }

    public boolean isLatestRevision() {
        return latestRevision;
    }

    public void setLatestRevision(boolean latestRevision) {
        this.latestRevision = latestRevision;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public PdpPolicy getParentPolicy() {
        return parentPolicy;
    }

    public void setParentPolicy(PdpPolicy parentPolicy) {
        this.parentPolicy = parentPolicy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static PdpPolicy revision(String newName, PdpPolicy parent, String newPolicyXml, String userIdentifier, String userDisplayName, boolean isActive) {
        parent.getRevisions().forEach(p -> p.setLatestRevision(false));
        parent.setLatestRevision(false);

        PdpPolicy clone = new PdpPolicy();
        clone.setPolicyXml(newPolicyXml);
        clone.setName(newName);
        clone.setLatestRevision(true);
        clone.setUserIdentifier(userIdentifier);
        clone.setAuthenticatingAuthority(parent.getAuthenticatingAuthority());
        clone.setUserDisplayName(userDisplayName);
        clone.setActive(isActive);
        clone.setRevisionNbr(parent.getRevisions().size() + 1);
        clone.setPolicyId(PolicyTemplateEngine.getPolicyId(newName));
        clone.setType(parent.getType());

        parent.addRevision(clone);

        return clone;
    }
}
