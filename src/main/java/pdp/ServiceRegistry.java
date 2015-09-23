package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceRegistry {

  private ObjectMapper objectMapper = new ObjectMapper();

  public List<EntityMetaData> serviceProviders() {
    return parseEntities("saml20-sp-remote.json");
  }

  public List<EntityMetaData> identityProviders() {
    return parseEntities("saml20-idp-remote.json");
  }

  private List<EntityMetaData> parseEntities(String path) {
    try {
      List<Map<String, Object>> list = objectMapper.readValue(new ClassPathResource(path).getInputStream(), List.class);
      Stream<EntityMetaData> entities = list.stream().map(entry ->
          new EntityMetaData((String) entry.get("entityid"), getDescription(entry)));
      return entities.collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getDescription(Map<String, Object> entry) {
    String name = null;
    Map<String, String> names = (Map<String, String>) entry.get("name");
    if (names != null) {
      name = names.get("en");
      if (name == null) {
        name = names.get("nl");
      }
    }
    return name;
  }

}
