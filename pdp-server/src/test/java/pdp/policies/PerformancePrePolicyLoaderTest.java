package pdp.policies;

import org.junit.Test;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.serviceregistry.TestingServiceRegistry;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PerformancePrePolicyLoaderTest {

  @Test
  public void testGetPolicies() throws Exception {
    PdpPolicyRepository policyRepository = mock(PdpPolicyRepository.class);
    PdpPolicyViolationRepository policyViolationRepository = mock(PdpPolicyViolationRepository.class);
    PerformancePrePolicyLoader subject = new PerformancePrePolicyLoader(0, new TestingServiceRegistry(), policyRepository, policyViolationRepository);

    List<PdpPolicy> policies = subject.getPolicies();
    assertEquals(3, policies.size());

    subject.loadPolicies();
    verify(policyRepository, times(3)).save(any(PdpPolicy.class));

  }

}