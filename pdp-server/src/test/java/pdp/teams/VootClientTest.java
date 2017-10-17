package pdp.teams;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.std.pip.engines.ConfigurableEngine;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VootClientTest {

    private VootClient subject = new VootClient(new RestTemplate(), "http://localhost:8889");

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @BeforeClass
    public static void doBeforeClass() {
      System.setProperty("http.keepAlive", "false");
    }

    @Test
    public void testInstanceOf() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> classForEngine = Class.forName("pdp.teams.TeamsPIP");
        ConfigurableEngine teamsPIP = ConfigurableEngine.class.cast(classForEngine.newInstance());

        System.out.println(teamsPIP instanceof VootClientAware);
    }

    @Test
    public void testGroups() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("voot/empty_groups.json").getInputStream());
        stubFor(get(urlEqualTo("/internal/groups/id1")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)));
        List<String> groups = subject.groups("id1");
        assertTrue(groups.isEmpty());
    }

    @Test
    public void testHasAccess() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("voot/groups.json").getInputStream());
        stubFor(get(urlEqualTo("/internal/groups/id1")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)));
        List<String> groups = subject.groups("id1");
        assertEquals(14, groups.size());
        assertTrue(groups.stream().allMatch(group -> StringUtils.hasText(group)));
    }

}
