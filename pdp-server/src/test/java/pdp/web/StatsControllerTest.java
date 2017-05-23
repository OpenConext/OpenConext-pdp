package pdp.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import pdp.AbstractPdpIntegrationTest;
import pdp.domain.PdpDecision;
import pdp.repositories.PdpDecisionRepository;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev","no-csrf"})
public class StatsControllerTest extends AbstractPdpIntegrationTest {

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private PdpDecisionRepository pdpDecisionRepository;

    @Before
    public void before() throws IOException {
        super.before();
        addShibHeaders();
        addDecisions();
    }

    private void addDecisions() {
        pdpDecisionRepository.deleteAll();
        pdpDecisionRepository.save(new PdpDecision("{\"serviceProvicer\":\"https://profile.test.surfconext.nl/simplesaml/module.php/saml/sp/metadata.php/default-sp\",\"identityProvider\":\"https://openidp.feide.no\",\"responseTimeMs\":68,\"pipResponses\":{},\"decision\":\"Permit\"}"));
        pdpDecisionRepository.save(new PdpDecision("{\"serviceProvicer\":\"https://profile.test.surfconext.nl/simplesaml/module.php/saml/sp/metadata.php/default-sp\",\"identityProvider\":\"https://openidp.feide.no\",\"responseTimeMs\":68,\"pipResponses\":{},\"decision\":\"Permit\"}"));
    }

    @Test
    public void testDecisions() {
        List<PdpDecision> decisions = getForObject("/internal/decisions?daysAgo=10", new ParameterizedTypeReference<List<PdpDecision>>() {
        });
        assertEquals(2, decisions.size());
    }

    @Override
    public TestRestTemplate getRestTemplate() {
        return restTemplate;
    }


}