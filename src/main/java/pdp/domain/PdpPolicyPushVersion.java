package pdp.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pdp.xacml.PolicyTemplateEngine;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Date;

@Entity(name = "pdp_policy_push_version")
@Getter
@Setter
@NoArgsConstructor
public class PdpPolicyPushVersion {

    @Id
    private Long id;

    @Column
    private Long version;

    @Column(name = "updated_at")
    private Instant updatedAt;

}
