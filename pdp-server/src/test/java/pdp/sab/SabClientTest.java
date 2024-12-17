package pdp.sab;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;

public class SabClientTest {

    private final SabClient subject = new SabClientConfig()
            .sabClient("user", "password", "http://localhost:8889");

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Test
    public void testGetRolesHappyFlow() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sab/response_success.json").getInputStream(), UTF_8);
        stubFor(get(urlPathEqualTo("/api/profile"))
                .withQueryParam("uid", equalTo("penny"))
                .withQueryParam("idp", equalTo("surfnet.nl"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
        List<String> roles = subject.roles("urn:collab:person:surfnet.nl:penny");
        assertEquals(Arrays.asList(
                        "Instellingsbevoegde", "DNS-Beheerder", "SURFconextbeheerder", "SURFopzichter-beheerder", "Superuser"),
                roles);
    }

    @Test
    public void testGetRolesFailures() throws Exception {
        //if something goes wrong, we just don't get roles and SAB policies will return Indeterminate. We log all requests and responses
        for (String fileName : Arrays.asList("response_acl_blocked.json", "response_invalid_user.json", "response_unknown_user.json")) {
            //lambda requires error handling
            assertEmptyRoles(fileName);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRolesIncorrectURN() {
        subject.roles("nope");
    }

    @Test
    public void testMockSabClient() throws IOException {
        SabClient sabClient = new SabClientConfig().mockSabClient("user", "password", "http://localhost");
        assertEquals(2, sabClient.roles(URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN).size());
        assertEquals(0, sabClient.roles("Nope").size());
    }

    private void assertEmptyRoles(String fileName) throws IOException {
        String response = IOUtils.toString(new ClassPathResource("sab/" + fileName).getInputStream(), UTF_8);
        stubFor(get(urlPathEqualTo("/api/profile"))
                .withQueryParam("uid", equalTo("penny"))
                .withQueryParam("idp", equalTo("surfnet.nl"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
        assertTrue(subject.roles("urn:collab:person:surfnet.nl:penny").isEmpty());

    }
}