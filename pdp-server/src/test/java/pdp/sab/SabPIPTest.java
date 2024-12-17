package pdp.sab;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SabPIPTest {

    private SabPIP sabPIP;

    @Before
    public void setUp() {
        sabPIP = new SabPIP();
        sabPIP.setSabClient(new SabClientConfig().sabClient("userName", "password", "http://nope"));
    }

    @Test
    public void testGetDescription() {
        assertEquals("Sab Policy Information Point", sabPIP.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testGetAttributes() {
        sabPIP.getAttributes("urn");
    }
}