package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public interface JsonMapper {

    ObjectMapper objectMapper = ObjectMapperWrapper.init();

    class ObjectMapperWrapper {
        private static com.fasterxml.jackson.databind.ObjectMapper init() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.registerModule(new Jdk8Module());
            return objectMapper;
        }
    }

}