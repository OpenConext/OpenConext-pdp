package pdp.web;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PdpControllerTest {

  @Test
  public void test() throws Exception {
    Iterator<String> iterator = Arrays.asList("1", "2", "3").iterator();
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
        .collect(Collectors.toCollection(ArrayList::new));

    //System.out.println(collect);
  }
}
