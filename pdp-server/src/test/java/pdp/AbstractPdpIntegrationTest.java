package pdp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
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

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
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
  protected static final String policyIdToDelete = "urn:surfconext:xacml:policy:id:_open_conextpdp_deny_rule_policy_empty_permit";

  protected static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  protected PdpPolicyViolationRepository pdpPolicyViolationRepository;

  @Autowired
  protected PdpPolicyRepository pdpPolicyRepository;

  @Value("${local.server.port}")
  protected int port;

  protected HttpHeaders headers;

  protected final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();
  protected final PdpPolicyDefinitionParser policyDefinitionParser = new PdpPolicyDefinitionParser();
  protected final PolicyLoader policyLoader = new DevelopmentPrePolicyLoader(new ClassPathResource("xacml/policies"), mock(PdpPolicyRepository.class), mock(PdpPolicyViolationRepository.class));

  @BeforeClass
  public static void beforeClass() {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Before
  public void before() throws IOException {
    headers = new HttpHeaders();
    headers.set(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    pdpPolicyViolationRepository.deleteAll();
  }

  //to differentiate between the Basic Auth and the shib restTemplate
  public abstract RestTemplate getRestTemplate();

  protected JsonPolicyRequest getJsonPolicyRequest() throws IOException {
    return objectMapper.readValue(new ClassPathResource("xacml/requests/base_request.json").getInputStream(), JsonPolicyRequest.class);
  }

  protected void addShibHeaders() {
    headers.set(UID_HEADER_NAME, "urn:collab:person:example.com:admin");
    headers.set(SHIB_AUTHENTICATING_AUTHORITY, PolicyLoader.authenticatingAuthority);
    headers.set(DISPLAY_NAME_HEADER_NAME, "John Doe");
  }

  protected void impersonate(String idp, String nameId, String displayName) {
    headers.set(X_IDP_ENTITY_ID, idp);
    headers.set(X_UNSPECIFIED_NAME_ID, nameId);
    headers.set(X_DISPLAY_NAME, displayName);
    headers.set(X_IMPERSONATE, "true");
  }

  protected ResponseEntity<String> getImpersonated(String path, String idp) {
    impersonate(idp, URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN, "John Doe");
    return doExchange(path, new HttpEntity<>(headers), HttpMethod.GET);
  }

  protected ResponseEntity<String> get(String path) {
    return doExchange(path, new HttpEntity<>(headers), HttpMethod.GET);
  }

  protected Set<HttpMethod> options(String path) {
    ResponseEntity<String> response = doExchange(path, new HttpEntity<>(headers), HttpMethod.OPTIONS);
    return response.getHeaders().getAllow();
  }

  protected <T> T getForObject(String path, ParameterizedTypeReference<T> responseType) {
    return doExchangeForObject(path, new HttpEntity<>(headers), HttpMethod.GET, responseType);
  }

  protected ResponseEntity<String> post(String path, Object requestBody) {
    try {
      String jsonRequest = objectMapper.writeValueAsString(requestBody);
      return doExchange(path, new HttpEntity<>(jsonRequest, headers), HttpMethod.POST);
    } catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }

  protected <T> T postForObject(String path, Object requestObject, ParameterizedTypeReference<T> responseType) {
    try {
      String requestBody = objectMapper.writeValueAsString(requestObject);
      return doExchangeForObject(path, new HttpEntity<>(requestBody, headers), HttpMethod.POST, responseType);
    } catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }

  private URI getServerUri(String path) {
    checkArgument(path.startsWith("/"));

    return URI.create("http://localhost:" + port + "/pdp/api" + path);
  }

  protected ResponseEntity<String> delete(String path) {
    return doExchange(path, new HttpEntity<>(headers), HttpMethod.DELETE);
  }

  protected ResponseEntity<String> doExchange(String path, HttpEntity<?> requestEntity, HttpMethod method) {
    return getRestTemplate().exchange(getServerUri(path), method, requestEntity, String.class);
  }

  protected <T> T doExchangeForObject(String path, HttpEntity<?> requestEntity, HttpMethod method, ParameterizedTypeReference<T> responseType) {
    return getRestTemplate().exchange(getServerUri(path), method, requestEntity, responseType).getBody();
  }

  protected PdpPolicy setUpViolation(String policyId) {
    PdpPolicy policy = getExistingPolicy(policyId);
    pdpPolicyViolationRepository.save(new PdpPolicyViolation(policy, "json", "response", true));
    return policy;
  }

  protected PdpPolicy getExistingPolicy(String id) {
    return pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(id, true).get(0);
  }

  protected PdpPolicy getExistingPolicy() {
    return getExistingPolicy(policyId);
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
