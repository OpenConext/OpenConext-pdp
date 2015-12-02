package pdp.repositories;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import pdp.domain.PdpPolicy;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static pdp.xacml.PolicyTemplateEngine.getPolicyId;

public class PdpPolicyRepositoryTest extends AbstractRepositoryTest {

  @Before
  public void before() throws Exception {
    pdpPolicyRepository.save(pdpPolicy(NAME_ID + 1));
  }

  @Test
  public void testFindByName() throws JsonProcessingException {
    Optional<PdpPolicy> policy = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(getPolicyId(NAME_ID + 1), true).stream().findFirst();
    assertEquals(NAME_ID + 1, policy.get().getName());

    Optional<PdpPolicy> notLatestRevision = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(getPolicyId(NAME_ID + 1), false).stream().findFirst();
    assertFalse(notLatestRevision.isPresent());
  }

  @Test
  public void testFindByNameNotFound() throws JsonProcessingException {
    Optional<PdpPolicy> policy = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision("nope", true).stream().findFirst();
    assertFalse(policy.isPresent());
  }

  @Test
  public void testFindRevisionCountPerId() throws Exception {
    PdpPolicy policy = pdpPolicy(NAME_ID + 2);
    PdpPolicy.revision(NAME_ID + 3, policy, "xml", "system", "http://mock-idp", "John Doe");
    PdpPolicy.revision(NAME_ID + 4, policy, "xml", "system", "http://mock-idp", "John Doe");
    pdpPolicyRepository.save(policy);

    PdpPolicy latestRevision = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(getPolicyId(NAME_ID + 4), true).get(0);

    List<Object[]> revisionCountPerId = pdpPolicyRepository.findRevisionCountPerId();
    Map<Number, Number> revisionCountPerIdMap = revisionCountPerId.stream().collect(toMap((obj) -> (Number) obj[0], (obj) -> (Number) obj[1]));

    assertEquals("2", revisionCountPerIdMap.get(latestRevision.getId().intValue()).toString());
  }

}

