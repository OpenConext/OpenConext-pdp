package pdp.sab;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SabResponseParserTest {

    private SabResponseParser subject = new SabResponseParser();

    @Test
    public void testParse() throws IOException, XMLStreamException {
        InputStream soap = new ClassPathResource("sab/response_success.xml").getInputStream();
        List<String> roles = subject.parse(soap);
        assertEquals(Arrays.asList(
            "Superuser", "Instellingsbevoegde", "Infraverantwoordelijke", "OperationeelBeheerder", "Mailverantwoordelijke",
            "Domeinnamenverantwoordelijke", "DNS-Beheerder", "AAIverantwoordelijke", "Beveiligingsverantwoordelijke"),
            roles);
    }

}