package pdp.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PdpAttributeTest {

    @Test
    public void testEquals() throws Exception {
        PdpAttribute attr1 = new PdpAttribute("some_name", "some_value");
        PdpAttribute attr2 = new PdpAttribute("some_name", "some_value");

        assertEquals(attr1, attr2);

        assertEquals(1, new HashSet<>(Arrays.asList(attr1, attr2)).size());

        String s = attr1.toString();

        assertTrue(s.contains("some_name"));
        assertTrue(s.contains("some_value"));
    }

    @Test
    public void testValueTrim() {
        PdpAttribute nullCheck = new PdpAttribute("some_name", null);
        assertNull(nullCheck.getValue());

        PdpAttribute attr = new PdpAttribute("some_name", "  123  ");
        assertEquals("123", attr.getValue());
    }
}