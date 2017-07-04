package pdp.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddressConverter {

    public long convert(String host) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException();
        }
        byte[] octets = inetAddress.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

}
