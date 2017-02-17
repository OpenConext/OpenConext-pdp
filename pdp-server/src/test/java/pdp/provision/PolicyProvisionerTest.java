package pdp.provision;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class PolicyProvisionerTest {

    private PdpPolicyRepository pdpPolicyRepository;
    private PolicyProvisioner subject;

    @Before
    public void before() {
        pdpPolicyRepository = mock(PdpPolicyRepository.class);
        subject = new PolicyProvisioner("test-provisioned-policies", pdpPolicyRepository);
    }

    @Test
    public void onApplicationEvent() throws Exception {
        when(pdpPolicyRepository.findByNameAndLatestRevision("test-policy", true)).thenReturn(Optional.empty());

        ArgumentCaptor<PdpPolicy> argument = ArgumentCaptor.forClass(PdpPolicy.class);

        subject.onApplicationEvent(null);

        verify(pdpPolicyRepository).save(argument.capture());
        PdpPolicy policy = argument.getValue();
        assertEquals("urn:surfconext:xacml:policy:id:test_policy", policy.getPolicyId());
        assertTrue(policy.getPolicyXml()
            .contains("DataType=\"http://www.w3.org/2001/XMLSchema#string\">" +
                "urn:collab:group:surfteams.nl:nl:surfnet:diensten:team_name" +
                "</AttributeValue>"));
    }

    @Test(expected = RuntimeException.class)
    public void pathDoesNotExists() throws Exception {
        subject = new PolicyProvisioner("does-not-exists", pdpPolicyRepository);
        subject.onApplicationEvent(null);
    }

}