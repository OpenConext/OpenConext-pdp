package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

public interface JsonMapper {

  ObjectMapper objectMapper = ObjectMapperWrapper.init();

  class ObjectMapperWrapper {
    private static com.fasterxml.jackson.databind.ObjectMapper init() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new AfterburnerModule());
      return objectMapper;
    }
  }

}