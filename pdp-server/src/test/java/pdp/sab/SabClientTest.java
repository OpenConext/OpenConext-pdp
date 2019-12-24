package pdp.sab;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;

public class SabClientTest {

    private SabClient subject = new SabClientConfig().sabClient("user", "password", "http://localhost:8889/sab");

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Test
    public void testGetRolesHappyFlow() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sab/response_success.xml").getInputStream(), "UTF-8");
        stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
            .willReturn(aResponse().withStatus(200).withBody(response)));
        List<String> roles = subject.roles("id1");
        assertEquals(Arrays.asList(
            "Superuser", "Instellingsbevoegde", "Infraverantwoordelijke", "OperationeelBeheerder", "Mailverantwoordelijke",
            "Domeinnamenverantwoordelijke", "DNS-Beheerder", "AAIverantwoordelijke", "Beveiligingsverantwoordelijke"),
            roles);
    }

    @Test
    public void testGetRolesFailures() throws Exception {
        //if something goes wrong, we just don't get roles and SAB policies will return Indeterminate. We log all requests and responses
        for (String fileName : Arrays.asList("response_acl_blocked.xml", "response_invalid_user.xml", "response_unknown_user.xml")) {
            //lambda requires error handling
            assertEmptyRoles(fileName);
        }
    }

    @Test
    public void testMockSabClient() throws IOException {
        SabClient sabClient = new SabClientConfig().mockSabClient("user", "password", "http://localhost");
        assertEquals(2, sabClient.roles(URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN).size());
        assertEquals(0, sabClient.roles("Nope").size());
    }

    private void assertEmptyRoles(String fileName) throws IOException {
        String response = IOUtils.toString(new ClassPathResource("sab/" + fileName).getInputStream(), "UTF-8");
        stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
            .willReturn(aResponse().withStatus(200).withBody(response)));
        assertTrue(subject.roles("id1").isEmpty());

    }
}