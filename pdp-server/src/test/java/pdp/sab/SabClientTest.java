package pdp.sab;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;

public class SabClientTest {

  private SabClient subject = new SabClient("user", "password", "http://localhost:8889/sab");

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Test
  public void testGetRolesHappyFlow() throws Exception {
    String response = IOUtils.toString(new ClassPathResource("sab/response_success.xml").getInputStream());
    stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
        .willReturn(aResponse().withStatus(200).withBody(response)));
    List<String> roles = subject.roles("id1");
    assertEquals(Arrays.asList(
        "Superuser", "Instellingsbevoegde", "Infraverantwoordelijke", "OperationeelBeheerder", "Mailverantwoordelijke",
        "Domeinnamenverantwoordelijke", "DNS-Beheerder", "AAIverantwoordelijke", "Beveiligingsverantwoordelijke"),
        roles);
  }
}