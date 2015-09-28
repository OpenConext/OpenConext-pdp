package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicyDefinition;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.teams.VootClientConfig;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;
import static pdp.xacml.PdpPolicyDefinitionParser.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"xacml.properties.path=classpath:xacml.conext.properties", "spring.profiles.active=dev"})
public class PdpApplicationTest {

  @Autowired
  private PdpPolicyRepository pdpPolicyRepository;

  @Autowired
  private PdpPolicyViolationRepository pdpPolicyViolationRepository;

  @Value("${local.server.port}")
  private int port;
  private MultiValueMap<String, String> headers;
  private TestRestTemplate restTemplate = new TestRestTemplate("pdp-admin", "secret");
  private ObjectMapper objectMapper = new ObjectMapper();
  private PdpPolicyDefinitionParser policyDefinitionParser = new PdpPolicyDefinitionParser();

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
    pdpPolicyViolationRepository.deleteAll();
//    pdpPolicyRepository.deleteAll();
//    pdpPolicyRepository.save(Arrays.asList(
//        new PdpPolicy(IOUtils.toString(new ClassPathResource("SURFconext.SURFspotAccess.xml").getInputStream()), "SURFspotAccess"),
//        new PdpPolicy(IOUtils.toString(new ClassPathResource("SURFconext.TeamAccess.xml").getInputStream()), "TeamAccess")));
//    pdpPolicyViolationRepository.deleteAll();
  }

//  @Test
//  public void test_surfspot_permit() throws Exception {
//    doDecide("SURFspotAccess.Permit.json", Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
//  }
//
//  @Test
//  public void test_surfspot_permit_with_categories_shorthand() throws Exception {
//    doDecide("SURFspotAccess.Permit.CategoriesShorthand.json", Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
//  }
//
//  @Test
//  public void test_surfspot_indeterminate_no_policy_matches() throws Exception {
//    doDecide("SURFspotAccess.Indeterminate.json", Decision.INDETERMINATE, "urn:oasis:names:tc:xacml:1.0:status:processing-error");
//  }
//
//  @Test
//  public void test_surfspot_no_eduperson_attribute() throws Exception {
//    doDecide("SURFspotAccess.Missing.EduPerson.json", Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
//  }
//
//  @Test
//  public void test_surfspot_deny() throws Exception {
//    doDecide("SURFspotAccess.Deny.json", Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
//  }
//
//  @Test
//  public void test_teams_pip_deny() throws Exception {
//    doDecide("TeamAccess.Deny.json", Decision.DENY, "urn:oasis:names:tc:xacml:1.0:status:ok");
//    String associatedAdviceId = "urn:unique:advice:reasonForDeny";
//    //test the repo
//    List<PdpPolicyViolation> violations = pdpPolicyViolationRepository.findByAssociatedAdviceId(associatedAdviceId);
//    assertEquals(1, violations.size());
//    //test the repo for countBy
//    Long count = pdpPolicyViolationRepository.countByAssociatedAdviceId(associatedAdviceId);
//    assertEquals(1L, count.longValue());
//  }
//
//  @Test
//  public void test_teams_pip_approve() throws Exception {
//    doDecide("TeamAccess.Permit.json", Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
//  }
//
//  @Test
//  public void test_teams_pip_no_name_id() throws Exception {
//    doDecide("TeamAccess.NoNameId.json", Decision.INDETERMINATE, "urn:oasis:names:tc:xacml:1.0:status:missing-attribute");
//  }

  @Test
  public void tmp() throws Exception {
    doDecide("xacml/requests/avans_request.json", Decision.DENY,"");
//    JsonPolicyRequest policyRequest = objectMapper.readValue(new ClassPathResource("xacml/requests/Request_SURFnet.json").getInputStream(), JsonPolicyRequest.class);
//    ClassPathResource classPathResource = new ClassPathResource("xacml/policies/OpenConext.pdp.avans.IDPandGroupClause.xml");
//    PdpPolicyDefinition definition = policyDefinitionParser.parse(classPathResource.getFilename(), IOUtils.toString(classPathResource.getInputStream()));
//    JsonPolicyRequest permitPolicyRequest = policyRequest.copy();
//    permitPolicyRequest.addOrReplaceResourceAttribute(SP_ENTITY_ID, definition.getServiceProviderId());
//    if (definition.getIdentityProviderIds().isEmpty()) {
//      permitPolicyRequest.deleteAttribute(IDP_ENTITY_ID);
//    } else {
//      permitPolicyRequest.addOrReplaceResourceAttribute(IDP_ENTITY_ID,definition.getIdentityProviderIds().get(0));
//    }
//    Map<String, List<PdpAttribute>> groupedAttributes = definition.getAttributes().stream().collect(Collectors.groupingBy(PdpAttribute::getName));
//    Set<Map.Entry<String, List<PdpAttribute>>> entries = groupedAttributes.entrySet();
//    entries.forEach(entry -> permitPolicyRequest.addOrReplaceAccessSubjectAttribute(entry.getKey(), entry.getValue().get(0).getValue()));
//    if (definition.getAttributes().stream().filter(pdpAttribute -> pdpAttribute.getName().equalsIgnoreCase(GROUP_URN)).findFirst().isPresent()) {
//      permitPolicyRequest.addOrReplaceAccessSubjectAttribute(NAME_ID, URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN);
//    }
//    postDecide(permitPolicyRequest, Decision.PERMIT, "urn:oasis:names:tc:xacml:1.0:status:ok");
  }

  private void postDecide(JsonPolicyRequest policyRequest, Decision expectedDecision, String statusCodeValue) throws Exception {
    final String url = "http://localhost:" + port + "/pdp/api/decide/policy";
    String jsonRequest = objectMapper.writeValueAsString(policyRequest);
    HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);
    String jsonResponse = restTemplate.postForObject(url, request, String.class);
    Response response = JSONResponse.load(jsonResponse);
    assertEquals(1, response.getResults().size());
    Result result = response.getResults().iterator().next();
    assertEquals(expectedDecision, result.getDecision());
    assertEquals(statusCodeValue, result.getStatus().getStatusCode().getStatusCodeValue().getUri().toString());
  }

  private void doDecide(String requestJsonFile, Decision expectedDecision, String statusCodeValue) throws Exception {
    final String url = "http://localhost:" + port + "/pdp/api/decide/policy";
    String jsonRequest = IOUtils.toString(new ClassPathResource(requestJsonFile).getInputStream());
    HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);
    String jsonResponse = restTemplate.postForObject(url, request, String.class);
    Response response = JSONResponse.load(jsonResponse);
    assertEquals(1, response.getResults().size());
    Result result = response.getResults().iterator().next();
    assertEquals(expectedDecision, result.getDecision());
    assertEquals(statusCodeValue, result.getStatus().getStatusCode().getStatusCodeValue().getUri().toString());
  }

  private <T> Collector<T, List<T>, T> singletonCollector() {
    return Collector.of(ArrayList::new, List::add, (left, right) -> {
          left.addAll(right);
          return left;
        }, list -> {
          if (list.isEmpty()) {
            return null;
          }
          return list.get(0);
        }
    );
  }

}
