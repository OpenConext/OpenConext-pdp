package pdp.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyViolation;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;

public class PdpPolicyViolationRepositoryTest extends AbstractRepositoryTest {

  private PdpPolicy pdpPolicy;

  @Before
  public void before() throws Exception {
    pdpPolicy = pdpPolicyRepository.save(pdpPolicy(NAME_ID + 1));
    Timestamp oneMonthAgo = new Timestamp(System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30));
    IntStream.of(1, 2, 2, 3, 3, 3).forEach(i -> {
      PdpPolicyViolation violation = new PdpPolicyViolation(pdpPolicy, "{}", "response", true);
      pdpPolicy.addPdpPolicyViolation(violation);
      violation.setCreated(oneMonthAgo);
      pdpPolicyViolationRepository.save(violation);
    });
  }

  @Test
  public void testFindCountPerPolicyId() throws JsonProcessingException {
    List<Object[]> countPerPolicyId = pdpPolicyViolationRepository.findCountPerPolicyId();
    Map<Number, Number> countPerPolicyIdMap = countPerPolicyId.stream().collect(toMap((obj) -> (Number) obj[0], (obj) -> (Number) obj[1]));

    assertEquals(countPerPolicyIdMap.get(pdpPolicy.getId()), 6L);
  }

  @Test
  public void testDeleteByPolicyId() {
    long before = pdpPolicyViolationRepository.count();
    pdpPolicyRepository.delete(pdpPolicy);
    long after = pdpPolicyViolationRepository.count();
    assertEquals(before - 6, after);
  }

  @Test
  public void retentionPeriod() {
    int deleted = pdpPolicyViolationRepository.deleteOlderThenRetentionDays(25);
    assertEquals(6, deleted);
  }

}
