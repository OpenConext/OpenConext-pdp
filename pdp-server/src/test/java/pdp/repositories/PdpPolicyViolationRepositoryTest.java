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
import pdp.domain.PdpPolicyViolation;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.Assert.assertEquals;

public class PdpPolicyViolationRepositoryTest extends AbstractRepositoryTest {

  @Before
  public void before() throws Exception {
    IntStream.of(1, 2, 2, 3, 3, 3).forEach(i -> {
      String policyId = PolicyTemplateEngine.getPolicyId(POLICY_ID + i);
      pdpPolicyViolationRepository.save(new PdpPolicyViolation(policyId, POLICY_ID + i, "{}", "response"));
    });
  }

  @Test
  public void testFindCountPerPolicyId() throws JsonProcessingException {
    List<Object[]> countPerPolicyId = pdpPolicyViolationRepository.findCountPerPolicyId();
    Map<String, List<Object[]>> grouped = countPerPolicyId.stream().filter(res -> String.class.cast(res[0]).startsWith(PolicyTemplateEngine.getPolicyId(POLICY_ID))).collect(groupingBy(o -> (String) o[0]));
    assertEquals(3, grouped.size());

    LongStream.of(1, 2, 3).forEach(i -> assertEquals(i, grouped.get(PolicyTemplateEngine.getPolicyId(POLICY_ID + i)).get(0)[1]));
  }

  @Test
  public void testFindByPolicyId() {
    List<PdpPolicyViolation> byPolicyId = pdpPolicyViolationRepository.findByPolicyId(PolicyTemplateEngine.getPolicyId(POLICY_ID + 3));
    assertEquals(3, byPolicyId.size());
  }

}
