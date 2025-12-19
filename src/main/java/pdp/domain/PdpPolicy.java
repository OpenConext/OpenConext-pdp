package pdp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import pdp.xacml.PolicyTemplateEngine;

import java.util.Date;

@Entity(name = "pdp_policies")
public class PdpPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String policyId;

    @Getter
    @Column
    private String policyXml;

    @Getter
    @Column
    private String name;

    @Getter
    @Column(name = "is_active")
    private boolean active;

    @Column
    private String type;

    @Column(nullable = false, name = "ts")
    private Date created;

    public PdpPolicy() {
    }

    public PdpPolicy(String policyXml, String name, boolean active, String type) {
        this.policyXml = policyXml;
        this.name = name;
        this.active = active;
        this.policyId = PolicyTemplateEngine.getPolicyId(name);
        this.type = type;
        this.created = new Date();
    }

}
