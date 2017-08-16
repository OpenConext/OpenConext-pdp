package pdp.web;

import org.junit.Test;
import pdp.domain.Validation;
import pdp.ip.IPInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidationControllerTest {

    private ValidationController subject = new ValidationController();

    @Test
    public void validation() throws Exception {
        assertTrue(subject.validation(new Validation("ip", "fe80:0:0:0:0:0:c0a8:11")));
        assertTrue(subject.validation(new Validation("ip", "192.168.2.255")));
        assertFalse(subject.validation(new Validation("ip", "nope")));
    }

    @Test
    public void ipInfo() throws Exception {
        assertIpInfo(
            subject.ipInfo("fe80:0:0:0:0:0:c0a8:11", 32),
            "fe80:0:ffff:ffff:ffff:ffff:ffff:ffff",
            "fe80:0:0:0:0:0:0:0",
            7.922816251426434E28D,
            false);

        assertIpInfo(
            subject.ipInfo("192.168.6.56", 21),
            "192.168.7.255",
            "192.168.0.0",
            2048D,
            true);

    }

    private void assertIpInfo(IPInfo ipInfo, String broadCastAddress, String networkAddress, double capacity, boolean isIpv4) {
        assertEquals(broadCastAddress, ipInfo.getBroadcastAddress());
        assertEquals(networkAddress, ipInfo.getNetworkAddress());
        assertEquals(capacity, ipInfo.getCapacity(), 1D);
        assertEquals(isIpv4, ipInfo.isIpv4());
    }

}