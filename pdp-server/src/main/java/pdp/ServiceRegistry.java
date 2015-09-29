package pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pdp.xacml.PdpPolicyDefinitionParser.IDP_ENTITY_ID;
import static pdp.xacml.PdpPolicyDefinitionParser.SP_ENTITY_ID;

@RestController
@RequestMapping(headers = {"content-type=application/json"}, produces = {"application/json"})
public class ServiceRegistry {

  private ObjectMapper objectMapper = new ObjectMapper();
  private List<String> allowedLanguages = Arrays.asList("en", "nl");
  private Map<String, List<EntityMetaData>> entityMetaData = new HashMap<>();

  public ServiceRegistry() {
    entityMetaData.put(IDP_ENTITY_ID, parseEntities("service-registry/saml20-idp-remote.json"));
    entityMetaData.put(SP_ENTITY_ID, parseEntities("service-registry/saml20-sp-remote.json"));
  }

  @RequestMapping(method = RequestMethod.GET, value = "/internal/serviceProviders")
  public List<EntityMetaData> serviceProviders() {
    return entityMetaData.get(SP_ENTITY_ID);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/internal/identityProviders")
  public List<EntityMetaData> identityProviders() {
    return entityMetaData.get(IDP_ENTITY_ID);
  }

  private List<EntityMetaData> parseEntities(String path) {
    try {
      List<Map<String, Object>> list = objectMapper.readValue(new ClassPathResource(path).getInputStream(), List.class);
      return list.stream().map(entry ->
              new EntityMetaData(
                  (String) entry.get("entityid"),
                  getMetaDateEntry(entry, "en", "description"),
                  getMetaDateEntry(entry, "en", "name"),
                  getMetaDateEntry(entry, "nl", "description"),
                  getMetaDateEntry(entry, "nl", "name")
              )
      ).collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getMetaDateEntry(Map<String, Object> entry, String lang, String description1) {
    lang = allowedLanguages.contains(lang.toLowerCase()) ? lang : "en";
    String description = null;
    Map<String, String> descriptions = (Map<String, String>) entry.get(description1);
    if (descriptions != null) {
      description = descriptions.get(lang.toLowerCase());
      if (description == null) {
        // try the other language
        description = descriptions.get(lang.equalsIgnoreCase("nl") ? "en" : "nl");
      }
    }
    return description;
  }

}
