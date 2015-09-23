package pdp;

import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyViolation;
import pdp.repositories.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"xacml.properties.path=classpath:xacml.conext.test.database.properties", "spring.profiles.active=dev"})
public class PdpApplicationTest {

  @Autowired
  private PdpPolicyRepository pdpPolicyRepository;

  @Autowired
  private PdpPolicyViolationRepository pdpPolicyViolationRepository;

  @Value("${local.server.port}")
  protected int port;
  private MultiValueMap<String, String> headers;
  protected TestRestTemplate client = new TestRestTemplate("pdp_admin", "secret");

  @Before
  public void before() throws IOException {
    headers = new LinkedMultiValueMap<>();
    headers.add("Content-Type", "application/json");

    /*
     * We can't use Transactional rollback as the Application runs in a different process. This would only
     * work if we would test against the local PdpPolicyRepository - and this is an Integration test.
     *
     * For this to work we have configured the OpenConextEvaluationContextFactory not to cache policies but
     * to retrieve them from the database each request (e.g. openconext.pdp.cachePolicies=false)
     */
    pdpPolicyRepository.deleteAll();
    pdpPolicyRepository.save(Arrays.asList(
        new PdpPolicy(IOUtils.toString(new ClassPathResource("SURFconext.SURFspotAccess.xml").getInputStream()), "SURFspotAccess"),
        new PdpPolicy(IOUtils.toString(new ClassPathResource("SURFconext.TeamAccess.xml").getInputStream()), "TeamAccess")));
    pdpPolicyViolationRepository.deleteAll();
  }

  @Test
  public void test_surfspot_permit() throws Exception {
    doDecide("SURFspotAccess.Permit.json", Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  @Test
  public void test_surfspot_permit_with_categories_shorthand() throws Exception {
    doDecide("SURFspotAccess.Permit.CategoriesShorthand.json", Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  @Test
  public void test_surfspot_indeterminate_no_policy_matches() throws Exception {
    doDecide("SURFspotAccess.Indeterminate.json", Decision.INDETERMINATE, "urn:oasis:names:tc:xacml:1.0:status:processing-error");
  }

  @Test
  public void test_surfspot_no_eduperson_attribute() throws Exception {
    doDecide("SURFspotAccess.Missing.EduPerson.json", Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  @Test
  public void test_surfspot_deny() throws Exception {
    doDecide("SURFspotAccess.Deny.json", Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  @Test
  public void test_teams_pip_deny() throws Exception {
    doDecide("TeamAccess.Deny.json", Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
    List<PdpPolicyViolation> violations = pdpPolicyViolationRepository.findByAssociatedAdviceId("urn:unique:advice:reasonForDeny");
    assertEquals(1, violations.size());
  }

  @Test
  public void test_teams_pip_approve() throws Exception {
    doDecide("TeamAccess.Permit.json", Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  @Test
  public void test_teams_pip_no_name_id() throws Exception {
    doDecide("TeamAccess.NoNameId.json", Decision.INDETERMINATE, "urn:oasis:names:tc:xacml:1.0:status:missing-attribute");
  }

  private void doDecide(String requestJsonFile, Decision expectedDecision, String statusCodeValue) throws Exception {
    final String url = "http://localhost:" + port + "/decide";
    String jsonRequest = IOUtils.toString(new ClassPathResource(requestJsonFile).getInputStream());
    HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);
    String jsonResponse = client.postForObject(url, request, String.class);
    Response response = JSONResponse.load(jsonResponse);
    assertEquals(1, response.getResults().size());
    Result result = response.getResults().iterator().next();
    assertEquals(expectedDecision, result.getDecision());
    assertEquals(statusCodeValue, result.getStatus().getStatusCode().getStatusCodeValue().getUri().toString());
  }


}
