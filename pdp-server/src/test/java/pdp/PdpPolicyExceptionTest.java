package pdp;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PdpPolicyExceptionTest {

  @Test(expected = RuntimeException.class)
  public void testGetDetailsException() throws Exception {
    new PdpPolicyException("1");
  }

  @Test
  public void testGetDetails() throws Exception {
    assertEquals(Collections.singletonMap("1", "2"), new PdpPolicyException("1", "2").getDetails());
  }
}