package pdp.repositories;


import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pdp.domain.PdpPolicy;

import static pdp.policies.PolicyLoader.authenticatingAuthority;
import static pdp.policies.PolicyLoader.userDisplayName;
import static pdp.policies.PolicyLoader.userIdentifier;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Transactional //rollback commits
public abstract class AbstractRepositoryTest {

    protected static final String NAME_ID = "name_id_";

    @Autowired
    protected PdpPolicyRepository pdpPolicyRepository;

    @Autowired
    protected PdpPolicyViolationRepository pdpPolicyViolationRepository;

    @Autowired
    protected PdpDecisionRepository pdpDecisionRepository;

    protected PdpPolicy pdpPolicy(String name) {
        return new PdpPolicy("xml", name, true, userIdentifier, authenticatingAuthority, userDisplayName, true);
    }

}
