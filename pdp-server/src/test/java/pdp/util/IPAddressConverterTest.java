package pdp.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class IPAddressConverterTest {

    private IPAddressConverter subject = new IPAddressConverter();

    @Test
    public void convert() {
        long lower = subject.convert("194.171.175.0");
        long upper = subject.convert("194.171.175.255");
        long ip = subject.convert("194.171.175.176");

        assertTrue(ip >= lower && ip <= upper);
    }

    @Test
    public void convertIpv6() {
        long lower = subject.convert("FEDC:BA98:7654:3210:FEDC:BA98:7654:3210");
        long upper = subject.convert("FEDC:BA98:7654:3210:FEDC:BA98:7654:3210");
        long ip = subject.convert("FEDC:BA98:7654:3210:FEDC:BA98:7654:3210");

        assertTrue(ip >= lower && ip <= upper);
    }

}