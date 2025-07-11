package pdp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.base.Throwables;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
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

/**
 * Note this class is slow. it starts up the entire Spring boot app.
 * <p/>
 * If you want to test policies quickly then see StandAlonePdpEngineTest
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractPdpIntegrationTest implements JsonMapper {

    protected static final String policyId = "urn:surfconext:xacml:policy:id:open_conextpdp_single_attribute";

    @Autowired
    protected PdpPolicyViolationRepository pdpPolicyViolationRepository;

    @Autowired
    protected PdpPolicyRepository pdpPolicyRepository;

    @LocalServerPort
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
    public abstract TestRestTemplate getRestTemplate();

    protected JsonPolicyRequest getJsonPolicyRequest() throws IOException {
        return objectMapper.readValue(new ClassPathResource("xacml/requests/base_request.json").getInputStream(), JsonPolicyRequest.class);
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
        return pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(id, true).get();
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
