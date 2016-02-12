package pdp.serviceregistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.springframework.data.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import pdp.access.PolicyIdpAccessUnknownIdentityProvidersException;
import pdp.domain.EntityMetaData;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static pdp.util.StreamUtils.singletonOptionalCollector;
import static pdp.xacml.PdpPolicyDefinitionParser.IDP_ENTITY_ID;
import static pdp.xacml.PdpPolicyDefinitionParser.SP_ENTITY_ID;

public class ClassPathResourceServiceRegistry implements ServiceRegistry {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final static ObjectMapper objectMapper = new ObjectMapper();
  private Map<String, List<EntityMetaData>> entityMetaData = new HashMap<>();

  public ClassPathResourceServiceRegistry(boolean initialize) {
    //this provides subclasses a hook to set properties before initializing metadata
    if (initialize) {
      initializeMetadata();
    }
  }

  protected void initializeMetadata() {
    entityMetaData = new HashMap<>();
    entityMetaData.put(IDP_ENTITY_ID, mapResources(getIdpResources()));
    entityMetaData.put(SP_ENTITY_ID, mapResources(getSpResources()));
    LOG.debug("Initialized SR Resources. Number of IDPs {}. Number of SPs {}", entityMetaData.get(IDP_ENTITY_ID).size(), entityMetaData.get(SP_ENTITY_ID).size());
  }

  private List<EntityMetaData> mapResources(List<Resource> resources) {
    return resources.stream().map(resource -> parseEntities(resource)).flatMap(l -> l.stream()).collect(toList());
  }

  protected List<Resource> getIdpResources() {
    return doGetResources("service-registry/identity-providers.json");
  }

  protected List<Resource> getSpResources() {
    return doGetResources("service-registry/service-providers.json");
  }

  protected List<Resource> doGetResources(String... paths) {
    List<Resource> collect = asList(paths).stream().map(path -> new ClassPathResource(path)).collect(toList());
    return collect;
  }

  @Override
  public List<EntityMetaData> serviceProviders() {
    return entityMetaData.get(SP_ENTITY_ID);
  }

  @Override
  public List<EntityMetaData> identityProviders() {
    return entityMetaData.get(IDP_ENTITY_ID);
  }

  @Override
  public Set<EntityMetaData> identityProvidersByAuthenticatingAuthority(String authenticatingAuthority) {
    EntityMetaData idp = identityProviderByEntityId(authenticatingAuthority);
    String institutionId = idp.getInstitutionId();
    if (StringUtils.hasText(institutionId)) {
      return identityProviders().stream().filter(md -> institutionId.equals(md.getInstitutionId())).collect(toSet());
    } else {
      return Sets.newHashSet(idp);
    }
  }

  @Override
  public Set<EntityMetaData> serviceProvidersByInstitutionId(String institutionId) {
    if (StringUtils.isEmpty(institutionId)) {
      return Collections.emptySet();
    }
    return serviceProviders().stream().filter(sp -> institutionId.equals(sp.getInstitutionId())).collect(toSet());
  }

  @Override
  public Optional<EntityMetaData> serviceProviderOptionalByEntityId(String entityId) {
    return entityMetaDataOptionalByEntityId(entityId, serviceProviders());
  }

  @Override
  public Optional<EntityMetaData> identityProviderOptionalByEntityId(String entityId) {
    return entityMetaDataOptionalByEntityId(entityId, identityProviders());
  }

  private Optional<EntityMetaData> entityMetaDataOptionalByEntityId(String entityId, List<EntityMetaData> entityMetaDatas) {
    return entityMetaDatas.stream().filter(sp -> sp.getEntityId().equals(entityId)).collect(singletonOptionalCollector());
  }

  @Override
  public EntityMetaData serviceProviderByEntityId(String entityId) {
    return getOptionalEntityMetaData(entityId, serviceProviderOptionalByEntityId(entityId));
  }

  @Override
  public EntityMetaData identityProviderByEntityId(String entityId) {
    return getOptionalEntityMetaData(entityId, identityProviderOptionalByEntityId(entityId));
  }

  @Override
  public List<String> identityProviderNames(List<String> entityIds) {
    return identityProviders().stream().filter(idp -> entityIds.contains(idp.getEntityId())).map(EntityMetaData::getNameEn).collect(toList());
  }

  private EntityMetaData getOptionalEntityMetaData(String entityId, Optional<EntityMetaData> optional) {
    if (!optional.isPresent()) {
      throw new PolicyIdpAccessUnknownIdentityProvidersException(entityId + " is not a valid or known IdP / SP entityId");
    }
    return optional.get();
  }

  protected List<EntityMetaData> parseEntities(Resource resource) {
    try {
      List<Map<String, Object>> list = objectMapper.readValue(resource.getInputStream(), List.class);
      return list.stream().map(entry ->
          new EntityMetaData(
              (String) entry.get("entityid"),
              (String) entry.get("coin:institution_id"),
              getMetaDateEntry(entry, "description"),
              getMetaDateEntry(entry, "name"),
              getMetaDateEntry(entry, "description"),
              getMetaDateEntry(entry, "name"),
              getPolicyEnforcementDecisionRequired(entry),
              getAllowedAll(entry),
              getAllowedEntries(entry)
          )
      ).sorted(sortEntityMetaData()).collect(toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Set<String> getAllowedEntries(Map<String, Object> entry) {
    List<String> allowedEntities = (List<String>) entry.getOrDefault("allowedEntities", Collections.emptyList());
    return new HashSet<>(allowedEntities);
  }

  private boolean getAllowedAll(Map<String, Object> entry) {
    String allowedall = (String) entry.getOrDefault("allowedall", "yes");
    return allowedall.equals("yes");
  }

  private boolean getPolicyEnforcementDecisionRequired(Map<String, Object> entry) {
    Object policyEnforcementDecisionRequired = entry.get("coin:policy_enforcement_decision_required");
    if (policyEnforcementDecisionRequired != null) {
      if (policyEnforcementDecisionRequired instanceof Boolean) {
        return (Boolean) policyEnforcementDecisionRequired;
      } else if (policyEnforcementDecisionRequired instanceof String) {
        return policyEnforcementDecisionRequired.equals("1");
      }
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

  private String getMetaDateEntry(Map<String, Object> entry, String attributeName) {
    String attribute = (String) entry.get(attributeName + ":en");
    if (attribute == null) {
      // try the other language
      attribute = (String) entry.get(attributeName + ":nl");
    }
    return attribute;
  }

  /**
   * Not part of the ServiceRegistry contract, but used for testing
   */
  public void allowAll(boolean allowAll) {
    identityProviders().forEach(md -> doAllowAll(md, allowAll));
    serviceProviders().forEach(md -> doAllowAll(md, allowAll));
  }

  private void doAllowAll(EntityMetaData md, boolean allowAll) {
    try {
      ReflectionUtils.setField(EntityMetaData.class.getDeclaredField("allowedAll"), md, allowAll);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

}
