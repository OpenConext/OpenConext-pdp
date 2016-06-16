package pdp.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import pdp.PdpEngineTest;
import pdp.domain.PdpPolicyDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static pdp.domain.PdpPolicyDefinition.policyDefinition;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=dev"})
public class MockMailBoxTest extends PdpEngineTest {

  @Autowired
  private MailBox mailBox ;

  @Rule
  public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Test
  public void testDoSendMailOn() throws Exception {
    doSendMail();
  }

  private void doSendMail() throws InterruptedException {
    Map<String, List<PdpPolicyDefinition>> conflicts = new HashMap<>();
    conflicts.put("https://mock-sp", Arrays.asList(policyDefinition("sp1", asList("idp1", "idp2"))));
    mailBox.sendConflictsMail(conflicts);

    //we send async
    Thread.sleep(1500);

    assertEquals(0, greenMail.getReceivedMessages().length);
  }
}
