package pdp.repositories;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import pdp.PdpApplication;
import pdp.PolicyTemplateEngine;
import pdp.domain.PdpPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

public class PdpPolicyRepositoryTest extends AbstractRepositoryTest {

  @Before
  public void before() throws Exception {
    pdpPolicyRepository.save(new PdpPolicy("xml", NAME_ID + 1));
  }

  @Test
  public void testFindByName() throws JsonProcessingException {
    Optional<PdpPolicy> policy = pdpPolicyRepository.findFirstByPolicyId(PolicyTemplateEngine.getPolicyId(NAME_ID + 1)).stream().findFirst();
    assertEquals(NAME_ID + 1, policy.get().getName());
  }

  @Test
  public void testFindByNameNotFound() throws JsonProcessingException {
    Optional<PdpPolicy> policy = pdpPolicyRepository.findFirstByPolicyId("nope").stream().findFirst();
    assertFalse(policy.isPresent());
  }

}

