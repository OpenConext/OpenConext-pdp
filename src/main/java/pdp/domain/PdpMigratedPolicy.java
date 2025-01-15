package pdp.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pdp.xacml.PolicyTemplateEngine;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "pdp_migrated_policies")
@Getter
@Setter
@NoArgsConstructor
public class PdpMigratedPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String type;

    @Column(nullable = false, name = "ts")
    private Date created;

    public PdpMigratedPolicy(PdpPolicy pdpPolicy) {
        this(
                pdpPolicy.getPolicyXml(),
                pdpPolicy.getName(),
                pdpPolicy.getUserIdentifier(),
                pdpPolicy.getAuthenticatingAuthority(),
                pdpPolicy.getUserDisplayName(),
                pdpPolicy.getType()
        );
    }

    public PdpMigratedPolicy(String policyXml, String name, String userIdentifier, String authenticatingAuthority, String userDisplayName, String type) {
        this.policyXml = policyXml;
        this.name = name;
        this.userIdentifier = userIdentifier;
        this.userDisplayName = userDisplayName;
        this.authenticatingAuthority = authenticatingAuthority;
        this.policyId = PolicyTemplateEngine.getPolicyId(name);
        this.type = type;
        this.created = new Date();
    }
}
