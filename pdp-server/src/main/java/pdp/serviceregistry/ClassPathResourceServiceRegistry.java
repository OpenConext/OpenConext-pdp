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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static pdp.PdpApplication.singletonOptionalCollector;
import static pdp.xacml.PdpPolicyDefinitionParser.IDP_ENTITY_ID;
import static pdp.xacml.PdpPolicyDefinitionParser.SP_ENTITY_ID;
import static java.util.Comparator.*;
public class ClassPathResourceServiceRegistry implements ServiceRegistry {

  private final static ObjectMapper objectMapper = new ObjectMapper();
  private final static List<String> allowedLanguages = Arrays.asList("en", "nl");
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private Map<String, List<EntityMetaData>> entityMetaData = new HashMap<>();

  public ClassPathResourceServiceRegistry() {
    initializeMetadata();
  }

  protected void initializeMetadata() {
    try {
      lock.writeLock().lock();
      entityMetaData = new HashMap<>();
      entityMetaData.put(IDP_ENTITY_ID, parseEntities(getIdpResource()));
      entityMetaData.put(SP_ENTITY_ID, parseEntities(getSpResource()));
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected Resource getIdpResource() {
    return new ClassPathResource("service-registry/saml20-idp-remote.json");
  }

  protected Resource getSpResource() {
    return new ClassPathResource("service-registry/saml20-sp-remote.json");
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
    return (e1,e2) -> {
      String n1 = e1.getNameEn() != null ? e1.getNameEn() : e1.getNameNl() ;
      String n2 = e2.getNameEn() != null ? e2.getNameEn() : e2.getNameNl() ;
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
