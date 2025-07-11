package pdp.web;

import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import pdp.AbstractPdpIntegrationTest;

import static org.junit.Assert.*;

public class PdpControllerTest extends AbstractPdpIntegrationTest {

    @Test
    public void pushPolicyDefinitions() {
    }

    @Override
    public TestRestTemplate getRestTemplate() {
        return new TestRestTemplate("pdp_admin", "secret");
    }
}