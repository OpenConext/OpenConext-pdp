package pdp.policies;

import org.junit.Test;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.manage.ClassPathResourceManage;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PerformancePrePolicyLoaderTest {

    @Test
    public void testGetPolicies() throws Exception {
        PdpPolicyRepository policyRepository = mock(PdpPolicyRepository.class);
        PdpPolicyViolationRepository policyViolationRepository = mock(PdpPolicyViolationRepository.class);
        PerformancePrePolicyLoader subject = new PerformancePrePolicyLoader(0, new ClassPathResourceManage(), policyRepository, policyViolationRepository);

        List<PdpPolicy> policies = subject.getPolicies();
        assertEquals(51, policies.size());

        subject.loadPolicies();
        verify(policyRepository, times(51)).save(any(PdpPolicy.class));

    }

}