package pdp.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import pdp.JsonMapper;
import pdp.domain.PdpPolicyDefinition;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultMailBox implements MailBox, JsonMapper {

  @Autowired
  private JavaMailSender mailSender;
  private final String baseUrl;
  private final String to;
  private final String from;
  private final String subject = "PDP Conflicts";

  public DefaultMailBox(String baseUrl, String to, String from) {
    this.baseUrl = baseUrl;
    this.to = to;
    this.from = from;
  }

  @Override
  public void sendConflictsMail(Map<String, List<PdpPolicyDefinition>> conflicts) {
    try {
      Map<String, String> variables = new HashMap<>();
      variables.put("@@to@@", to);
      variables.put("@@conflicts@@", conflictsTable(conflicts));
      variables.put("@@base_url@@", baseUrl);
      sendMail("mail/conflicts.html", variables);
    } catch (MessagingException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String conflictsTable(Map<String, List<PdpPolicyDefinition>> conflicts) throws JsonProcessingException {
    return "<pre>" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(conflicts) + "</pre>";
  }

  private void sendMail(String templateName, Map<String, String> variables) throws MessagingException, IOException {
    String html = IOUtils.toString(new ClassPathResource(templateName).getInputStream());
    for (Map.Entry<String, String> var : variables.entrySet()) {
      html = html.replaceAll(var.getKey(), var.getValue());
    }
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setSubject(subject);
    helper.setTo(to);
    setText(html, helper);
    helper.setFrom(from);
    doSendMail(message);
  }

  protected void setText(String html, MimeMessageHelper helper) throws MessagingException {
    helper.setText(html, true);
  }

  protected void doSendMail(MimeMessage message) {
    new Thread(() -> mailSender.send(message)).start();
  }

}
