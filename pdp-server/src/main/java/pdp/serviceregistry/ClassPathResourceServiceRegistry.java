package pdp.serviceregistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import pdp.access.PolicyIdpAccessUnknownIdentityProvidersException;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static pdp.util.StreamUtils.singletonOptionalCollector;
import static pdp.xacml.PdpPolicyDefinitionParser.IDP_ENTITY_ID;
import static pdp.xacml.PdpPolicyDefinitionParser.SP_ENTITY_ID;

public class ClassPathResourceServiceRegistry implements ServiceRegistry {

  protected final static Logger LOG = LoggerFactory.getLogger(ClassPathResourceServiceRegistry.class);

  private final static ObjectMapper objectMapper = new ObjectMapper();
  private final static List<String> allowedLanguages = asList("en", "nl");
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private Map<String, List<EntityMetaData>> entityMetaData = new HashMap<>();
  private final String environment;

  public ClassPathResourceServiceRegistry(String environment) {
    this(environment, true);
  }

  protected ClassPathResourceServiceRegistry(String environment, boolean initialize) {
    this.environment = environment;
    //this provides subclasses a hook to set properties before initializing metadata
    if (initialize) {
      initializeMetadata();
    }
  }

  protected void initializeMetadata() {
    try {
      lock.writeLock().lock();
      entityMetaData = new HashMap<>();
      entityMetaData.put(IDP_ENTITY_ID, mapResources(getIdpResources()));
      entityMetaData.put(SP_ENTITY_ID, mapResources(getSpResources()));
      LOG.debug("Initialized SR Resources. Number of IDPs {}. Number of SPs {}", entityMetaData.get(IDP_ENTITY_ID).size(), entityMetaData.get(SP_ENTITY_ID).size());
    } finally {
      lock.writeLock().unlock();
    }
  }

  private List<EntityMetaData> mapResources(List<Resource> resources) {
    return resources.stream().map(resource -> parseEntities(resource)).flatMap(l -> l.stream()).collect(toList());
  }

  protected List<Resource> getIdpResources() {
    return doGetResources("service-registry/saml20-idp-remote.json", "service-registry/saml20-idp-remote." + environment + ".json");
  }

  protected List<Resource> getSpResources() {
    return doGetResources("service-registry/saml20-sp-remote.json", "service-registry/saml20-sp-remote." + environment + ".json");
  }

  protected List<Resource> doGetResources(String... paths) {
    List<Resource> collect = asList(paths).stream().map(path -> new ClassPathResource(path)).filter(resource -> resource.exists()).collect(toList());
    return collect;
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
        throw new PolicyIdpAccessUnknownIdentityProvidersException(authenticatingAuthority + " is not a valid or known IdentityProvider entityId");
      }
      EntityMetaData idp = metaDataOptional.get();
      String institutionId = idp.getInstitutionId();
      if (StringUtils.hasText(institutionId)) {
        return identityProviders().stream().filter(md -> institutionId.equals(md.getInstitutionId())).collect(toSet());
      } else {
        return new HashSet(asList(idp));
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Set<EntityMetaData> serviceProvidersByInstitutionId(String institutionId) {
    if (StringUtils.isEmpty(institutionId)) {
      return Collections.EMPTY_SET;
    }
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
              getMetaDateEntry(entry, "nl", "name"),
              getPolicyEnforcementDecisionRequired(entry),
              getAllowedAll(entry),
              getAllowedEntries(entry)
          )
      ).sorted(sortEntityMetaData()).collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Set<String> getAllowedEntries(Map<String, Object> entry) {
    List<String> allowedEntities = (List<String>) entry.getOrDefault("allowedEntities", Collections.EMPTY_LIST);
    return new HashSet<>(allowedEntities);
  }

  private boolean getAllowedAll(Map<String, Object> entry) {
    String allowedall = (String) entry.getOrDefault("allowedall", "yes");
    return allowedall.equals("yes");
  }

  private boolean getPolicyEnforcementDecisionRequired(Map<String, Object> entry) {
    Object coin = entry.get("coin");
    if (coin != null && coin instanceof Map) {
      Map<String, Object> coinMap = (Map) coin;
      Object policyEnforcementDecisionRequired = coinMap.get("policy_enforcement_decision_required");
      if (policyEnforcementDecisionRequired != null && policyEnforcementDecisionRequired instanceof Boolean) {
        return (Boolean) policyEnforcementDecisionRequired;
      }
      return false;
    }
    return false;

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

  private String getMetaDateEntry(Map<String, Object> entry, String lang, String attributeName) {
    lang = allowedLanguages.contains(lang.toLowerCase()) ? lang : "en";
    String attr = null;
    Map<String, String> attributes = (Map<String, String>) entry.get(attributeName);
    if (attributes != null) {
      attr = attributes.get(lang.toLowerCase());
      if (attr == null) {
        // try the other language
        attr = attributes.get(lang.equalsIgnoreCase("nl") ? "en" : "nl");
      }
    }
    return attr;
  }

}
