package pdp.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.apache.openaz.xacml.std.json.JSONStructureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import pdp.AbstractPdpIntegrationTest;
import pdp.PdpApplication;
import pdp.domain.*;
import pdp.policies.PolicyLoader;
import pdp.repositories.PdpDecisionRepository;
import pdp.teams.VootClientConfig;
import pdp.xacml.PolicyTemplateEngine;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static pdp.util.StreamUtils.singletonCollector;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=no-csrf"})
public class StatsControllerTest extends AbstractPdpIntegrationTest {

  private RestTemplate restTemplate = new TestRestTemplate();

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
    pdpDecisionRepository.save(new PdpDecision("does not matter here"));
    pdpDecisionRepository.save(new PdpDecision("whatever"));
  }

  @Test
  public void testDecisions() {
    List<PdpDecision> decisions = getForObject("/internal/decisions?daysAgo=10", new ParameterizedTypeReference<List<PdpDecision>>() {
    });
    assertEquals(2, decisions.size());
  }

  @Override
  public RestTemplate getRestTemplate() {
    return restTemplate;
  }


}