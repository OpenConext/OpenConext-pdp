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
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VootClientTest {

    private final VootClient subject = new VootClient(WebClient.builder().build(), "http://localhost:8889");

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @BeforeClass
    public static void doBeforeClass() {
        System.setProperty("http.keepAlive", "false");
    }

    @Test
    public void testInstanceOf() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> classForEngine = Class.forName("pdp.teams.TeamsPIP");
        ConfigurableEngine teamsPIP = ConfigurableEngine.class.cast(classForEngine.getDeclaredConstructor().newInstance());
        assertTrue(teamsPIP instanceof VootClientAware);
    }

    @Test
    public void testGroups() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("voot/empty_groups.json").getInputStream(), "UTF-8");
        stubFor(get(urlEqualTo("/internal/groups/id1")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)));
        List<String> groups = subject.groups("id1");
        assertTrue(groups.isEmpty());
    }

    @Test
    public void testHasAccess() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("voot/groups.json").getInputStream(), "UTF-8");
        stubFor(get(urlEqualTo("/internal/groups/id1")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)));
        List<String> groups = subject.groups("id1");
        assertEquals(14, groups.size());
        assertTrue(groups.stream().allMatch(group -> StringUtils.hasText(group)));
    }

}
