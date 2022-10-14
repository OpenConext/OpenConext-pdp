package pdp.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import pdp.AbstractPdpIntegrationTest;
import pdp.domain.PdpPolicyDefinition;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pdp.domain.PdpPolicyDefinition.policyDefinition;

@ActiveProfiles("mail")
public class DefaultMailBoxTest extends AbstractPdpIntegrationTest {

    @Autowired
    private MailBox mailBox;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    @Test
    public void testMailBox() throws Exception {

        Map<String, List<PdpPolicyDefinition>> conflicts = new HashMap<>();
        PdpPolicyDefinition pdpPolicyDefinition = policyDefinition(asList("sp1"), asList("idp1", "idp2"));
        pdpPolicyDefinition.setDescription("$");
        conflicts.put("https://mock-sp", Arrays.asList(pdpPolicyDefinition));
        mailBox.sendConflictsMail(conflicts);

        //we send async
        Thread.sleep(1500);

        MimeMessage mimeMessage = greenMail.getReceivedMessages()[0];
        String body = getBody(mimeMessage);

        assertTrue(body.contains("http://localhost:8001/#conflicts"));
        assertEquals("to@test.nl", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
    }


    @Override
    public TestRestTemplate getRestTemplate() {
        throw new RuntimeException("Not used");
    }
}
