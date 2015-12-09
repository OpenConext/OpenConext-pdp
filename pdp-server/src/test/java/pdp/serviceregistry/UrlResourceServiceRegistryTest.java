package pdp.serviceregistry;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.TestCase.assertEquals;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;

public class UrlResourceServiceRegistryTest {

  private String idpResponse;
  private String spResponse;

  private UrlResourceServiceRegistry subject;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);


  @Before
  public void before() throws IOException {
    this.idpResponse = IOUtils.toString(new ClassPathResource("service-registry/saml20-idp-remote.test.json").getInputStream());
    stubFor(get(urlEqualTo("/idp")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(idpResponse)));

    this.spResponse = IOUtils.toString(new ClassPathResource("service-registry/saml20-sp-remote.test.json").getInputStream());
    stubFor(get(urlEqualTo("/sp")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(spResponse)));

    stubFor(head(urlEqualTo("/idp")).withHeader(IF_MODIFIED_SINCE, notMatching("X")).willReturn(aResponse().withStatus(200)));

    this.subject = new UrlResourceServiceRegistry("http://localhost:8889/idp", "http://localhost:8889/sp", 10);
  }

  @Test
  public void testMetaData() throws Exception {
    assertEquals(1, subject.identityProviders().size());
    assertEquals(3, subject.serviceProviders().size());
  }

  @Test
  public void testInitializeMetaDataNotModifed() throws IOException {
    stubFor(get(urlEqualTo("/sp")).willReturn(aResponse().withStatus(500)));
    stubFor(head(urlEqualTo("/idp")).withHeader(IF_MODIFIED_SINCE, notMatching("X")).willReturn(aResponse().withStatus(304)));
    subject.initializeMetadata();
  }
}