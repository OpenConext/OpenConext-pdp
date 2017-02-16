package pdp.provision;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PolicyProvisionerTest {

    private PdpPolicyRepository pdpPolicyRepository;
    private PolicyProvisioner subject ;

    @Before
    public void before() {
        pdpPolicyRepository = mock(PdpPolicyRepository.class);
        subject= new PolicyProvisioner("test-provisioned-policies", pdpPolicyRepository);
    }

    @Test
    public void onApplicationEvent() throws Exception {
        when(pdpPolicyRepository.findByNameAndLatestRevision("test-policy", true)).thenReturn(Optional.empty());

        ArgumentCaptor<PdpPolicy> argument = ArgumentCaptor.forClass(PdpPolicy.class);

        subject.onApplicationEvent(null);

        verify(pdpPolicyRepository).save(argument.capture());
        assertEquals("urn:surfconext:xacml:policy:id:test_policy", argument.getValue().getPolicyId());
    }

}