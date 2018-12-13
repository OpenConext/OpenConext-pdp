package pdp.mail;

import org.apache.commons.io.FileUtils;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class MockMailBox extends DefaultMailBox {

    public MockMailBox(String baseUrl, String to, String from) {
        super(baseUrl, to, from);
    }

    @Override
    protected void doSendMail(MimeMessage message) {
        //nope
    }

    @Override
    protected void setText(String html, MimeMessageHelper helper) throws MessagingException {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("mac os x") && false) {
                openInBrowser(html);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openInBrowser(String text) throws IOException {
        File tempFile = File.createTempFile("javamail", ".html");
        FileUtils.writeStringToFile(tempFile, text, Charset.defaultCharset());
        Runtime.getRuntime().exec("open " + tempFile.getAbsolutePath());
    }
}
