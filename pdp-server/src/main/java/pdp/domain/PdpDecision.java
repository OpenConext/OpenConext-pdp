package pdp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "pdp_decisions")
public class PdpDecision {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String decisionJson;

    @Column
    private Timestamp created;

    public PdpDecision() {
    }

    public PdpDecision(String decisionJson) {
        this.decisionJson = decisionJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDecisionJson() {
        return decisionJson;
    }

    public void setDecisionJson(String decisionJson) {
        this.decisionJson = decisionJson;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }
}
