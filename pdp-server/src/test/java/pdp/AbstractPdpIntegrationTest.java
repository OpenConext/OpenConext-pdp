package pdp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.policies.DevelopmentPrePolicyLoader;
import pdp.policies.PolicyLoader;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.xacml.PdpPolicyDefinitionParser;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static pdp.access.FederatedUserBuilder.*;
import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;

/**
 * Note this class is slow. it starts up the entire Spring boot app.
 * <p/>
 * If you want to test policies quickly then see StandAlonePdpEngineTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=dev"})
public abstract class AbstractPdpIntegrationTest {

  protected static final String policyId = "urn:surfconext:xacml:policy:id:_open_conextpdp_single_attribute";

  protected PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();

  @Autowired
  protected PdpPolicyViolationRepository pdpPolicyViolationRepository;

  @Autowired
  protected PdpPolicyRepository pdpPolicyRepository;

  @Autowired
  protected DataSource dataSource;

  protected static ObjectMapper objectMapper = new ObjectMapper();

  @Value("${local.server.port}")
  protected int port;
  protected HttpHeaders headers;

  //use this one to mock EB and username / password authentication
  protected TestRestTemplate testRestTemplate = new TestRestTemplate("pdp-admin", "secret");

  //use this one to mock internal api calls that expects a shib user in the security context
  protected RestTemplate restTemplate = new RestTemplate();

  protected PdpPolicyDefinitionParser policyDefinitionParser = new PdpPolicyDefinitionParser();
  protected PolicyLoader policyLoader = new DevelopmentPrePolicyLoader(new ClassPathResource("xacml/policies"), mock(PdpPolicyRepository.class), mock(PdpPolicyViolationRepository.class));

  @BeforeClass
  public static void beforeClass() {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Before
  public void before() throws IOException {
    headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    pdpPolicyViolationRepository.deleteAll();
  }

  //to differentiate between the Basic Auth and the shib restTemplate
  public abstract RestTemplate getRestTemplate();

  protected JsonPolicyRequest getJsonPolicyRequest() throws IOException {
    return objectMapper.readValue(new ClassPathResource("xacml/requests/base_request.json").getInputStream(), JsonPolicyRequest.class);
  }

  protected CollectionType constructCollectionType(Class<?> elementClass) {
    return objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass);
  }

  protected ResponseEntity<String> getImpersonated(String path, String idp) {
    headers.add(X_IDP_ENTITY_ID, idp);
    headers.add(X_UNSPECIFIED_NAME_ID, URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN);
    headers.add(X_DISPLAY_NAME, "John Doe");
    headers.add(X_IMPERSONATE, "true");
    return doExchange(path, new HttpEntity<>(headers), HttpMethod.GET);
  }

  protected ResponseEntity<String> get(String path) {
    return doExchange(path, new HttpEntity<>(headers), HttpMethod.GET);
  }

  protected ResponseEntity<String> post(String path, Object requestBody) throws JsonProcessingException {
    String jsonRequest = objectMapper.writeValueAsString(requestBody);
    return doExchange(path, new HttpEntity<>(jsonRequest, headers), HttpMethod.POST);
  }

  protected ResponseEntity<String> doExchange(String path, HttpEntity<?> requestEntity, HttpMethod method) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return getRestTemplate().exchange("http://localhost:" + port + "/pdp/api/" + path, method, requestEntity, String.class);
  }

  protected PdpPolicy setUpViolation(String policyId) {
    PdpPolicy policy = getExistingPolicy();
    pdpPolicyViolationRepository.save(new PdpPolicyViolation(policy, "json", "response", true));
    return policy;
  }

  protected PdpPolicy getExistingPolicy() {
    return pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true).get(0);
  }

  protected PdpPolicyDefinition getPdpPolicyDefinitionFromExistingPolicy() {
    PdpPolicy policy = getExistingPolicy();
    return pdpPolicyDefinitionParser.parse(policy);
  }

  protected void assertPolicyIsDeleted(PdpPolicy policy) {
    try {
      ResponseEntity<String> response = get("/internal/policies/" + policy.getId());
      assertEquals(404, response.getStatusCode().value());
    } catch (HttpClientErrorException e) {
      assertEquals(404, e.getStatusCode().value());
    }
  }
}
