package pdp.util;

import org.junit.Test;

import java.net.UnknownHostException;

public class CIDRUtilsTest {
    @Test
    public void getNetworkAddress() throws Exception {
        info("fe80:0:0:0:0:0:c0a8:1/120", "fe80:0:0:0:0:0:c0a8:11");
        info("192.168.2.0/24", "192.168.2.256");

    }

    private void info(String cidr, String ipAddress) throws UnknownHostException {
        CIDRUtils cidrUtils = new CIDRUtils(cidr);
        String networkAddress = cidrUtils.getNetworkAddress();
        String broadcastAddress = cidrUtils.getBroadcastAddress();
        boolean inRange = cidrUtils.isInRange(ipAddress);

        System.out.println(networkAddress);
        System.out.println(broadcastAddress);
        System.out.println(inRange);
    }

}