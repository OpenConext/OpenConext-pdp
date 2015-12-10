package pdp.repositories;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import pdp.PdpApplication;
import pdp.domain.PdpPolicy;

import static pdp.policies.PolicyLoader.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=test"})
@Transactional //rollback commits
public abstract class AbstractRepositoryTest {

  protected static final String NAME_ID = "name_id_";

  @Autowired
  protected PdpPolicyRepository pdpPolicyRepository;

  @Autowired
  protected PdpPolicyViolationRepository pdpPolicyViolationRepository;

  protected PdpPolicy pdpPolicy(String name) {
    return new PdpPolicy("xml", name, true, userIdentifier, authenticatingAuthority, userDisplayName);
  }

}
