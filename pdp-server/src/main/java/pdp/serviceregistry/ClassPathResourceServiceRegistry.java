package pdp.serviceregistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static pdp.PdpApplication.singletonOptionalCollector;
import static pdp.xacml.PdpPolicyDefinitionParser.IDP_ENTITY_ID;
import static pdp.xacml.PdpPolicyDefinitionParser.SP_ENTITY_ID;

public class ClassPathResourceServiceRegistry implements ServiceRegistry {

  private final static ObjectMapper objectMapper = new ObjectMapper();
  private final static List<String> allowedLanguages = Arrays.asList("en", "nl");
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private Map<String, List<EntityMetaData>> entityMetaData = new HashMap<>();
  private final String environment;

  public ClassPathResourceServiceRegistry(String environment) {
    this.environment = environment;
    initializeMetadata();
  }

  protected void initializeMetadata() {
    try {
      lock.writeLock().lock();
      entityMetaData = new HashMap<>();
      entityMetaData.put(IDP_ENTITY_ID, getIdpResources().stream().map(resource -> parseEntities(resource)).flatMap(l -> l.stream()).collect(toList()));
      entityMetaData.put(SP_ENTITY_ID, getSpResources().stream().map(resource -> parseEntities(resource)).flatMap(l -> l.stream()).collect(toList()));
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected List<Resource> getIdpResources() {
    return doGetResources("service-registry/saml20-idp-remote.json", "service-registry/saml20-idp-remote." + environment + ".json");
  }

  protected List<Resource> getSpResources() {
    return doGetResources("service-registry/saml20-sp-remote.json", "service-registry/saml20-sp-remote." + environment + ".json");
  }

  private List<Resource> doGetResources(String defaultPath, String environmentPath) {
    ClassPathResource defaultIdps = new ClassPathResource(defaultPath);
    ClassPathResource environmentIdps = new ClassPathResource(environmentPath);
    if (environmentIdps.exists()) {
      return Arrays.asList(defaultIdps, environmentIdps);
    }
    return Arrays.asList(defaultIdps);
  }

  @Override
  public List<EntityMetaData> serviceProviders() {
    try {
      lock.readLock().lock();
      return entityMetaData.get(SP_ENTITY_ID);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public List<EntityMetaData> identityProviders() {
    try {
      lock.readLock().lock();
      return entityMetaData.get(IDP_ENTITY_ID);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Set<EntityMetaData> identityProvidersByAuthenticatingAuthority(String authenticatingAuthority) {
    try {
      lock.readLock().lock();
      Optional<EntityMetaData> metaDataOptional = identityProviders().stream().filter(idp -> idp.getEntityId().equals(authenticatingAuthority)).collect(singletonOptionalCollector());
      if (!metaDataOptional.isPresent()) {
        throw new IllegalArgumentException(authenticatingAuthority + " is not a valid or known IdentityProvider entityId");
      }
      String institutionId = metaDataOptional.get().getInstitutionId();
      if (StringUtils.hasText(institutionId)) {
        return identityProviders().stream().filter(md -> institutionId.equals(md.getInstitutionId())).collect(toSet());
      } else {
        return new HashSet(Arrays.asList(metaDataOptional.get()));
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Set<EntityMetaData> serviceProvidersByInstitutionId(String institutionId) {
    try {
      lock.readLock().lock();
      return serviceProviders().stream().filter(sp -> institutionId.equals(sp.getInstitutionId())).collect(toSet());
    } finally {
      lock.readLock().unlock();
    }
  }

  private List<EntityMetaData> parseEntities(Resource resource) {
    try {
      List<Map<String, Object>> list = objectMapper.readValue(resource.getInputStream(), List.class);
      return list.stream().map(entry ->
              new EntityMetaData(
                  (String) entry.get("entityid"),
                  getInstitutionId(entry),
                  getMetaDateEntry(entry, "en", "description"),
                  getMetaDateEntry(entry, "en", "name"),
                  getMetaDateEntry(entry, "nl", "description"),
                  getMetaDateEntry(entry, "nl", "name")
              )
      ).sorted(sortEntityMetaData()).collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Comparator<? super EntityMetaData> sortEntityMetaData() {
    return (e1, e2) -> {
      String n1 = e1.getNameEn() != null ? e1.getNameEn() : e1.getNameNl();
      String n2 = e2.getNameEn() != null ? e2.getNameEn() : e2.getNameNl();
      return n1 == null ? -1 : n2 == null ? -1 : n1.trim().compareTo(n2.trim());
    };
  }

  private String getInstitutionId(Map<String, Object> entry) {
    Object coin = entry.get("coin");
    if (coin != null && coin instanceof Map) {
      Map<String, Object> coinMap = (Map) coin;
      return (String) coinMap.get("institution_id");
    }
    return null;
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
