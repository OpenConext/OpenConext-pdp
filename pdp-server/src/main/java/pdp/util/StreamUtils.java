package pdp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;

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
          if (list.isEmpty()) {
            throw new RuntimeException("Expected at least one element in the List");
          }
          return list.get(0);
        }
    );
  }

}
