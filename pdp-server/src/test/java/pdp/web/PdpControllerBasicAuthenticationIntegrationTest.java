package pdp.web;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import pdp.AbstractPdpIntegrationTest;
import pdp.access.FederatedUserBuilder;
import pdp.domain.PdpPolicy;
import pdp.policies.PolicyLoader;

import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "no-csrf"})
public class PdpControllerBasicAuthenticationIntegrationTest extends AbstractPdpIntegrationTest {

    private String PASSWORD = "secret";
    private String PDP_USER = "pdp_admin";

    private TestRestTemplate restTemplate;

    @After
    public void clear() {
        this.restTemplate = null;
    }

    @Test
    public void shouldBeAbleToFetchPoliciesWithBasciAuthAndHeadersOnProtectedEndPoint() {
        basicAuthTemplate();
        impersonate(PolicyLoader.authenticatingAuthority, "urn:collab:person:example.com:mary.doe", "Mary Doe");

        ResponseEntity<String> response = get("/protected/policies");

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    public void shouldNotBeAbleToFetchPoliciesWithBasciAuthAndNoHeadersOnProtectedEndPoint() {
        basicAuthTemplate();
        ResponseEntity<String> response = get("/protected/policies");

        assertThat(response.getStatusCode(), is(FORBIDDEN));
    }

    @Test
    public void shouldNotBeAbleToCallProtectedEndPointWithShibHeaders() {
        noAuthTemplate();
        addShibHeaders();

        ResponseEntity<String> response = get("/protected/policies");

        assertThat(response.getStatusCode(), is(FORBIDDEN));
    }

    @Test
    public void optionsShouldShouldAtLeastContainGetMethod() {
        basicAuthTemplate();
        impersonate(PolicyLoader.authenticatingAuthority, "urn:collab:person:example.com:mary.doe", "Mary Doe");

        Set<HttpMethod> options = options("/protected/policies");

        assertThat(options, hasItem(HttpMethod.GET));
    }

    @Override
    public TestRestTemplate getRestTemplate() {
        return restTemplate;
    }

    private void basicAuthTemplate() {
        this.restTemplate = new TestRestTemplate(PDP_USER, PASSWORD);
    }

    private void noAuthTemplate() {
        this.restTemplate = new TestRestTemplate();
    }

}