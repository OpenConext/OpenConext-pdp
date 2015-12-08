package pdp.teams;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VootClientTest {

  private VootClient subject = new VootClient(new RestTemplate(), "http://localhost:8889");

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

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