package pdp.util;

import org.apache.el.stream.Stream;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static pdp.util.StreamUtils.iteratorToList;
import static pdp.util.StreamUtils.singletonCollector;
import static pdp.util.StreamUtils.singletonOptionalCollector;

public class StreamUtilsTest {

  @Test
  public void testSingletonOptionalCollector() throws Exception {
    assertEquals("1",asList("1").stream().collect(singletonOptionalCollector()).get());
    assertFalse(new ArrayList<String>().stream().collect(singletonOptionalCollector()).isPresent());
  }

  @Test
  public void testSingletonCollector() throws Exception {
    assertEquals("1",asList("1").stream().collect(singletonCollector()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingletonCollectorIllegal() throws Exception {
    new ArrayList<String>().stream().collect(singletonCollector());
  }

  @Test
  public void testIteratorToList() throws Exception {
    assertEquals("1", iteratorToList(asList("1").iterator()).get(0));
  }
}