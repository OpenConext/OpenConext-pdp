package pdp.sab;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SabPIPTest {

    private SabPIP sabPIP;

    @Before
    public void setUp() throws Exception {
        sabPIP = new SabPIP();
        sabPIP.setSabClient(new SabClientConfig().sabClient("userName", "password", "http://nope"));
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals("Sab Policy Information Point", sabPIP.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testGetAttributes() throws Exception {
        sabPIP.getAttributes("urn");
    }
}