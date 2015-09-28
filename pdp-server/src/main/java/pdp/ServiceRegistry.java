package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(headers = {"content-type=application/json"}, produces = {"application/json"})
public class ServiceRegistry {

  private ObjectMapper objectMapper = new ObjectMapper();
  private List<String> allowedLanguages = Arrays.asList("en", "nl");

  @RequestMapping(method = RequestMethod.GET, value = "/internal/serviceProviders")
  public List<EntityMetaData> serviceProviders(@RequestParam(value = "lang", defaultValue = "en") String lang) {
    return parseEntities("service-registry/saml20-sp-remote.json", lang);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/internal/identityProviders")
  public List<EntityMetaData> identityProviders(@RequestParam(value = "lang", defaultValue = "en") String lang) {
    return parseEntities("service-registry/saml20-idp-remote.json", lang);
  }

  private List<EntityMetaData> parseEntities(String path, String lang) {
    try {
      List<Map<String, Object>> list = objectMapper.readValue(new ClassPathResource(path).getInputStream(), List.class);
      return list.stream().map(entry ->
          new EntityMetaData((String) entry.get("entityid"), getDescription(entry, lang))).collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getDescription(Map<String, Object> entry, String lang) {
    lang = allowedLanguages.contains(lang.toLowerCase()) ? lang : "en";
    String name = null;
    Map<String, String> names = (Map<String, String>) entry.get("name");
    if (names != null) {
      name = names.get(lang.toLowerCase());
      if (name == null) {
        // try the other language
        name = names.get(lang.equalsIgnoreCase("nl") ? "en" : "nl");
      }
    }
    return name;
  }

}
