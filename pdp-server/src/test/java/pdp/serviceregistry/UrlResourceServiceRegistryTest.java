package pdp.serviceregistry;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.io.ClassPathResource;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static junit.framework.TestCase.assertEquals;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static pdp.util.StreamUtils.singletonCollector;

public class UrlResourceServiceRegistryTest {

    private UrlResourceServiceRegistry subject;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @BeforeClass
    public static void doBeforeClass() {
      System.setProperty("http.keepAlive", "false");
    }

    @Before
    public void before() throws IOException {
        doBefore("service-registry/identity-providers.json", "service-registry/service-providers.json");
    }

    private void doBefore(String idpPath, String spPath) throws IOException {
        String idpResponse = IOUtils.toString(new ClassPathResource(idpPath).getInputStream());
        stubFor(get(urlEqualTo("/idp")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(idpResponse)));

        String spResponse = IOUtils.toString(new ClassPathResource(spPath).getInputStream());
        stubFor(get(urlEqualTo("/sp")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(spResponse)));

        stubFor(head(urlEqualTo("/idp")).withHeader(IF_MODIFIED_SINCE, notMatching("X")).willReturn(aResponse().withStatus(200)));

        this.subject = new UrlResourceServiceRegistry("user", "password", "http://localhost:8889/idp", "http://localhost:8889/sp", 10);
    }

    @Test
    public void testMetaData() throws Exception {
        assertEquals(8, subject.identityProviders().size());
        assertEquals(24, subject.serviceProviders().size());
    }

    @Test
    public void testInitializeMetaDataNotModifed() throws Exception {
        stubFor(get(urlEqualTo("/sp")).willReturn(aResponse().withStatus(500)));
        stubFor(head(urlEqualTo("/idp")).withHeader(IF_MODIFIED_SINCE, notMatching("X")).willReturn(aResponse().withStatus(304)));
        stubFor(head(urlEqualTo("/sp")).withHeader(IF_MODIFIED_SINCE, notMatching("X")).willReturn(aResponse().withStatus(304)));
        subject.initializeMetadata();
        testMetaData();
    }

    @Test
    public void testInitializeMetaDataNoEndpoint() throws IOException {
        stubFor(get(urlEqualTo("/sp")).willReturn(aResponse().withStatus(500)));
        new UrlResourceServiceRegistry("u", "p", "http://localhost:9999/bogus", "http://localhost:9999/bogus", 10);
    }


    @Test
    public void testSorting() throws Exception {
        doBefore("service-registry-test/identity-providers.json", "service-registry-test/service-providers.json");
        List<EntityMetaData> identityProviders = subject.identityProviders();
        assertEquals("5", identityProviders.get(0).getNameEn());
        assertEquals("urn:test:idp:bas", identityProviders.get(identityProviders.size() - 1).getEntityId());

        List<EntityMetaData> serviceProviders = subject.serviceProviders();
        assertEquals("Bas Test SP | SURFnet", serviceProviders.get(0).getNameEn());
        assertEquals("tst4", serviceProviders.get(serviceProviders.size() - 1).getEntityId());
    }

    @Test
    public void testNoNameFallback() throws Exception {
        doBefore("service-registry-test/identity-providers.json", "service-registry-test/service-providers.json");
        List<EntityMetaData> identityProviders = subject.identityProviders();
        String idpEntityId = "https://beta.surfnet.nl/simplesaml/saml2/idp/metadata.php";
        EntityMetaData idp = identityProviders.stream().filter(metaData -> metaData.getEntityId().equals(idpEntityId)).collect(singletonCollector());
        assertEquals(idpEntityId, idp.getNameEn());
        assertEquals(idpEntityId, idp.getNameNl());
    }

    @Test
    public void testHealth() {
        Health health = subject.health();
        assertEquals(Status.UP, health.getStatus());
    }

}
