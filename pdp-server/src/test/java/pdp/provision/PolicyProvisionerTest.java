package pdp.provision;

import org.junit.Before;
import org.junit.Test;
import pdp.repositories.PdpPolicyRepository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class PolicyProvisionerTest {

    private PolicyProvisioner subject = new PolicyProvisioner("test-provisioned-policies", mock(PdpPolicyRepository.class));

    @Test
    public void onApplicationEvent() throws Exception {
        subject.onApplicationEvent(null);
    }

}