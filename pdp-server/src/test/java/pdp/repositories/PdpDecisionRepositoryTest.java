package pdp.repositories;

import org.junit.Before;
import org.junit.Test;
import pdp.JsonMapper;
import pdp.domain.PdpDecision;
import pdp.stats.StatsContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class PdpDecisionRepositoryTest extends AbstractRepositoryTest implements JsonMapper{

  @Before
  public void setUp() throws Exception {
    pdpDecisionRepository.deleteAll();
    pdpDecisionRepository.save(new PdpDecision("{\"serviceProvicer\":\"https://profile.test.surfconext.nl/simplesaml/module.php/saml/sp/metadata.php/default-sp\",\"identityProvider\":\"https://openidp.feide.no\",\"responseTimeMs\":68,\"pipResponses\":{},\"decision\":\"Permit\"}"));
  }

  @Test
  public void testFindByCreatedAfter() throws Exception {
    Date tenDaysAgo = Date.from(Instant.now().minus(10, ChronoUnit.DAYS));
    List<PdpDecision> decisions = pdpDecisionRepository.findByCreatedAfter(tenDaysAgo);
    assertEquals(1, decisions.size());

    Date future = Date.from(Instant.now().plus(10, ChronoUnit.DAYS));
    decisions = pdpDecisionRepository.findByCreatedAfter(future);
    assertEquals(0, decisions.size());
  }
}