package pdp.serviceregistry;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.TestCase.assertEquals;

public class UrlResourceServiceRegistryTest {

  private  static final String USER = "user";
  private static final String PASSWORD = "secret";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Test
  public void testMetaData() throws Exception {
    String idpResponse = IOUtils.toString(new ClassPathResource("service-registry/saml20-idp-remote.test.json").getInputStream());
    stubFor(get(urlEqualTo("/idp")).withHeader("Authorization", equalTo(getBasicAuthz()))
        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(idpResponse)));

    String spResponse = IOUtils.toString(new ClassPathResource("service-registry/saml20-sp-remote.test.json").getInputStream());
    stubFor(get(urlEqualTo("/sp")).withHeader("Authorization", equalTo(getBasicAuthz()))
        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(spResponse)));

    ServiceRegistry subject = new UrlResourceServiceRegistry("http://localhost:8889/idp", "http://localhost:8889/sp", USER, PASSWORD, 10, 10);

    assertEquals(1, subject.identityProviders().size());
    assertEquals(3, subject.serviceProviders().size());
  }

  private String getBasicAuthz() {
    String userpass = USER + ":" + PASSWORD;
    return "Basic " + new String(Base64.getEncoder().encodeToString(userpass.getBytes()));
  }
}