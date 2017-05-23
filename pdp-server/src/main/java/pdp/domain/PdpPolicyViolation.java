package pdp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

@Entity(name = "pdp_policy_violations")
public class PdpPolicyViolation {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    //to prevent cycles
    @JsonIgnore
    private PdpPolicy policy;

    @Column(nullable = false)
    private String jsonRequest;

    @Column(nullable = false)
    private String response;

    @Column
    private Timestamp created;

    @Column
    private boolean isPlayground;

    public PdpPolicyViolation() {
    }

    public PdpPolicyViolation(PdpPolicy policy, String jsonRequest, String response, boolean isPlayground) {
        this.policy = policy;
        this.jsonRequest = jsonRequest;
        this.response = response;
        this.isPlayground = isPlayground;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @JsonIgnore
    public PdpPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(PdpPolicy policy) {
        this.policy = policy;
    }

    public boolean isPlayground() {
        return isPlayground;
    }

    public void setPlayground(boolean playground) {
        isPlayground = playground;
    }

    public String getPolicyName() {
        return policy.getName();
    }
}
