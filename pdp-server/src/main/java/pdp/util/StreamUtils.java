package pdp.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.StreamSupport.stream;

public abstract class StreamUtils {

  public static <T> Collector<T, List<T>, Optional<T>> singletonOptionalCollector() {
    return Collector.of(ArrayList::new, List::add, (left, right) -> {
          left.addAll(right);
          return left;
        }, list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0))
    );
  }

  public static <T> Collector<T, List<T>, T> singletonCollector() {
    return Collector.of(ArrayList::new, List::add, (left, right) -> {
          left.addAll(right);
          return left;
        }, list -> {
          if (list.isEmpty() || list.size() > 1) {
            throw new RuntimeException("Expected only one element in the List");
          }
          return list.get(0);
        }
    );
  }

  public static  <T> List<T> iteratorToList(Iterator<T> iterator) {
    return stream(spliteratorUnknownSize(iterator, ORDERED), false).collect(toCollection(ArrayList::new));
  }

}
