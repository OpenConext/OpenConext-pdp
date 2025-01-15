package pdp.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pdp.domain.PdpPolicyPushVersion;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PdpPolicyPushVersionRepositoryTest {

    @Autowired
    protected PdpPolicyPushVersionRepository pdpPolicyPushVersionRepository;

    @Test
    public void incrementVersion() {
        PdpPolicyPushVersion policyPushVersion = pdpPolicyPushVersionRepository.findById(1L).get();
        pdpPolicyPushVersionRepository.incrementVersion();
        PdpPolicyPushVersion updatedPolicyPushVersion = pdpPolicyPushVersionRepository.findById(1L).get();
        assertEquals(policyPushVersion.getVersion() + 1L, updatedPolicyPushVersion.getVersion().longValue());
    }
}