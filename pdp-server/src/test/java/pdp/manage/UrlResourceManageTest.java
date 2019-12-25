package pdp.manage;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static junit.framework.TestCase.assertEquals;
import static pdp.util.StreamUtils.singletonCollector;

public class UrlResourceManageTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);
    private UrlResourceManage subject;

    @BeforeClass
    public static void doBeforeClass() {
        System.setProperty("http.keepAlive", "false");
    }

    @Before
    public void before() throws IOException {
        doBefore("manage/identity-providers.json", "manage/service-providers.json", "manage/relying-parties.json");
    }

    private void doBefore(String idpPath, String spPath, String rpPath) throws IOException {
        String idpResponse = IOUtils.toString(new ClassPathResource(idpPath).getInputStream(), "UTF-8");
        stubFor(post(urlEqualTo("/manage/api/internal/search/saml20_idp")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(idpResponse)));

        String spResponse = IOUtils.toString(new ClassPathResource(spPath).getInputStream(), "UTF-8");
        stubFor(post(urlEqualTo("/manage/api/internal/search/saml20_sp")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(spResponse)));

        String rpResponse = IOUtils.toString(new ClassPathResource(rpPath).getInputStream(), "UTF-8");
        stubFor(post(urlEqualTo("/manage/api/internal/search/oidc10_rp")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(rpResponse)));

        this.subject = new UrlResourceManage("user", "password", "http://localhost:8889");
    }

    @Test
    public void testMetaData() {
        assertEquals(13, subject.identityProviders().size());
        assertEquals(51, subject.serviceProviders().size());
    }

    @Test
    public void testInitializeMetaDataNoEndpoint() {
        stubFor(post(urlEqualTo("/manage/api/internal/search/saml20_sp")).willReturn(aResponse().withStatus(500)));
        new UrlResourceManage("u", "p", "http://localhost:9999/bogus");
    }


    @Test
    public void testSorting() {
        List<EntityMetaData> identityProviders = subject.identityProviders();

        String nameEn = identityProviders.get(0).getNameEn();
        assertEquals("ADFS 2012 test2.surfconext.nl", nameEn);

        String entityId = identityProviders.get(identityProviders.size() - 1).getEntityId();
        assertEquals("https://thki-sid.pt-48.utr.surfcloud.nl/ssp/saml2/idp/metadata.php", entityId);

        List<EntityMetaData> serviceProviders = subject.serviceProviders();

        nameEn = serviceProviders.get(0).getNameEn();
        assertEquals("Bart test RP", nameEn);

        entityId = serviceProviders.get(serviceProviders.size() - 1).getEntityId();
        assertEquals("https://rp/2", entityId);
    }

    @Test
    public void testNoNameFallback() {
        List<EntityMetaData> identityProviders = subject.identityProviders();
        String idpEntityId = "https://idp.mrvanes.com/saml2/idp/metadata.php";
        EntityMetaData idp = identityProviders.stream().filter(metaData -> metaData.getEntityId().equals(idpEntityId)
        ).collect(singletonCollector());
        assertEquals(idpEntityId, idp.getNameEn());
        assertEquals(idpEntityId, idp.getNameNl());
    }

    @Test
    public void testServiceProvidersByEntityIds() {
        Map<String, EntityMetaData> result = subject.serviceProvidersByEntityIds(Arrays.asList("https://rp/2", "http://mock-sp", "nope"));
        assertEquals(51, result.size());
    }

}
