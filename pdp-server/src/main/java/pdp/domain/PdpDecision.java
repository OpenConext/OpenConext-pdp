package pdp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Timestamp;

public class PdpDecision {

    private String decisionJson;

    public PdpDecision(String decisionJson) {
        this.decisionJson = decisionJson;
    }

    public String getDecisionJson() {
        return decisionJson;
    }

}
