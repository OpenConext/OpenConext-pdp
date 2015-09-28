package pdp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Date;

@Entity(name = "pdp_policy_violations")
public class PdpPolicyViolation {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String associatedAdviceId;

    @Column(nullable = false)
    private String jsonRequest;

    @Column()
    private Date created;

    public PdpPolicyViolation() {
    }

    public PdpPolicyViolation(String associatedAdviceId, String jsonRequest) {
        this.associatedAdviceId = associatedAdviceId;
        this.jsonRequest = jsonRequest;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssociatedAdviceId() {
        return associatedAdviceId;
    }

    public void setAssociatedAdviceId(String associatedAdviceId) {
        this.associatedAdviceId = associatedAdviceId;
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
}
