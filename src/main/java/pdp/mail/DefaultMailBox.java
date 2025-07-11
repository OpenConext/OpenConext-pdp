package pdp.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import pdp.JsonMapper;
import pdp.domain.PdpPolicyDefinition;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultMailBox implements MailBox, JsonMapper {

    private final String baseUrl;
    private final String to;
    private final String from;
    @Autowired
    private JavaMailSender mailSender;

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
            variables.put("@@conflicts@@", objectToTable(conflicts));
            variables.put("@@base_url@@", baseUrl);
            sendMail("mail/conflicts.html", "PDP Conflicts", variables);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendInvalidPoliciesMail(List<PdpPolicyDefinition> invalids) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("@@to@@", to);
            variables.put("@@invalids@@", objectToTable(invalids));
            variables.put("@@base_url@@", baseUrl);
            sendMail("mail/invalids.html", "PDP Invalid Policies", variables);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String objectToTable(Object arg) throws JsonProcessingException {
        return "<pre>" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arg) + "</pre>";
    }

    private void sendMail(String templateName, String subject, Map<String, String> variables) throws
            MessagingException, IOException, jakarta.mail.MessagingException {
        String html = IOUtils.toString(new ClassPathResource(templateName).getInputStream(), Charset.defaultCharset());
        for (Map.Entry<String, String> var : variables.entrySet()) {
            String value = var.getValue();
            value = value.replaceAll("\\$", "\\\\\\$");
            html = html.replaceAll(var.getKey(), value);
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setTo(to);
        setText(html, mimeMessageHelper);
        mimeMessageHelper.setFrom(from);
        doSendMail(message);
    }

    protected void setText(String html, MimeMessageHelper helper) throws jakarta.mail.MessagingException {
        helper.setText(html, true);
    }

    protected void doSendMail(MimeMessage message) {
        new Thread(() -> mailSender.send(message)).start();
    }

}
