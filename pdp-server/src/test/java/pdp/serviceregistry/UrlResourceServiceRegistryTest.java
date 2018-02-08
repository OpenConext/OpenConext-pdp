package pdp.serviceregistry;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.io.ClassPathResource;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static junit.framework.TestCase.assertEquals;
import static pdp.util.StreamUtils.singletonCollector;

public class UrlResourceServiceRegistryTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);
    private UrlResourceServiceRegistry subject;

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
        stubFor(post(urlEqualTo("/manage/api/internal/search/saml20_idp")).willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json").withBody(idpResponse)));

        String spResponse = IOUtils.toString(new ClassPathResource(spPath).getInputStream());
        stubFor(post(urlEqualTo("/manage/api/internal/search/saml20_sp")).willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json").withBody(spResponse)));

        this.subject = new UrlResourceServiceRegistry("user", "password", "http://localhost:8889", 10);
    }

    @Test
    public void testMetaData() throws Exception {
        assertEquals(13, subject.identityProviders().size());
        assertEquals(49, subject.serviceProviders().size());
    }

    @Test
    public void testInitializeMetaDataNoEndpoint() throws IOException {
        stubFor(post(urlEqualTo("/manage/api/internal/search/saml20_sp")).willReturn(aResponse().withStatus(500)));
        new UrlResourceServiceRegistry("u", "p", "http://localhost:9999/bogus", 10);
    }


    @Test
    public void testSorting() throws Exception {
        doBefore("service-registry/identity-providers.json", "service-registry/service-providers.json");
        List<EntityMetaData> identityProviders = subject.identityProviders();

        String nameEn = identityProviders.get(0).getNameEn();
        assertEquals("ADFS 2012 test2.surfconext.nl", nameEn);

        String entityId = identityProviders.get(identityProviders.size() - 1).getEntityId();
        assertEquals("https://thki-sid.pt-48.utr.surfcloud.nl/ssp/saml2/idp/metadata.php", entityId);

        List<EntityMetaData> serviceProviders = subject.serviceProviders();

        nameEn = serviceProviders.get(0).getNameEn();
        assertEquals("Bart test RP", nameEn);

        entityId = serviceProviders.get(serviceProviders.size() - 1).getEntityId();
        assertEquals("https://thki-sid.pt-48.utr.surfcloud.nl/ssp/module.php/saml/sp/metadata.php/default-sp", entityId);
    }

    @Test
    public void testNoNameFallback() throws Exception {
        doBefore("service-registry/identity-providers.json", "service-registry/service-providers.json");
        List<EntityMetaData> identityProviders = subject.identityProviders();
        String idpEntityId = "https://idp.mrvanes.com/saml2/idp/metadata.php";
        EntityMetaData idp = identityProviders.stream().filter(metaData -> metaData.getEntityId().equals(idpEntityId)
        ).collect(singletonCollector());
        assertEquals(idpEntityId, idp.getNameEn());
        assertEquals(idpEntityId, idp.getNameNl());
    }

    @Test
    public void testHealth() {
        Health health = subject.health();
        assertEquals(Status.UP, health.getStatus());
    }

}
