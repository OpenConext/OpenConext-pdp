package pdp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import pdp.domain.*;
import pdp.policies.DevelopmentPrePolicyLoader;
import pdp.policies.PolicyLoader;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.xacml.PdpPolicyDefinitionParser;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;
import static pdp.xacml.PdpPolicyDefinitionParser.*;

/**
 * Note this class is slow. it starts up the entire Spring boot app.
 * <p/>
 * If you want to test policies quickly then see StandAlonePdpEngineTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"xacml.properties.path=classpath:xacml.conext.properties", "spring.profiles.active=dev"})
public class AbstractPdpIntegrationTest {

  @Autowired
  protected PdpPolicyViolationRepository pdpPolicyViolationRepository;

  @Autowired
  protected PdpPolicyRepository pdpPolicyRepository;

  protected static ObjectMapper objectMapper = new ObjectMapper();

  @Value("${local.server.port}")
  protected int port;
  protected MultiValueMap<String, String> headers;

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
    headers = new LinkedMultiValueMap<>();
    headers.add("Content-Type", "application/json");
  }

  protected JsonPolicyRequest getJsonPolicyRequest() throws IOException {
    return objectMapper.readValue(new ClassPathResource("xacml/requests/base_request.json").getInputStream(), JsonPolicyRequest.class);
  }

  protected CollectionType constructCollectionType(Class<?> elementClass) {
    return objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass);
  }

  @Test
  public void dummyToSatisfyIde() {}
}
