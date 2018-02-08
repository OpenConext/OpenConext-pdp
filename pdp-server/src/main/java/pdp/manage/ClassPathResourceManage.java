package pdp.manage;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import pdp.JsonMapper;
import pdp.access.PolicyIdpAccessUnknownIdentityProvidersException;
import pdp.domain.EntityMetaData;
import pdp.policies.PolicyMissingServiceProviderValidator;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static pdp.util.StreamUtils.singletonOptionalCollector;
import static pdp.xacml.PdpPolicyDefinitionParser.IDP_ENTITY_ID;
import static pdp.xacml.PdpPolicyDefinitionParser.SP_ENTITY_ID;

public class ClassPathResourceManage implements Manage, JsonMapper {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private Map<String, List<EntityMetaData>> entityMetaData = new ConcurrentHashMap<>();

    @Autowired
    private PolicyMissingServiceProviderValidator policyMissingServiceProviderValidator;

    public ClassPathResourceManage(boolean initialize) {
        //this provides subclasses a hook to set properties before initializing metadata
        if (initialize) {
            initializeMetadata();
        }
    }

    protected void initializeMetadata() {
        Map<String, List<EntityMetaData>> newEntityMetaData = new ConcurrentHashMap<>();
        newEntityMetaData.put(IDP_ENTITY_ID, parseEntities(getIdpResource()));
        newEntityMetaData.put(SP_ENTITY_ID, parseEntities(getSpResource()));
        this.entityMetaData = newEntityMetaData;
        LOG.debug("Initialized SR Resources. Number of IDPs {}. Number of SPs {}", entityMetaData.get(IDP_ENTITY_ID)
            .size(), entityMetaData.get(SP_ENTITY_ID).size());

        if (this.policyMissingServiceProviderValidator != null) {
            this.policyMissingServiceProviderValidator.validate();
        }
    }

    protected Resource getIdpResource() {
        return new ClassPathResource("service-registry/identity-providers.json");
    }

    protected Resource getSpResource() {
        return new ClassPathResource("service-registry/service-providers.json");
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
            return identityProviders().stream().filter(md -> institutionId.equals(md.getInstitutionId())).collect
                (toSet());
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

    private Optional<EntityMetaData> entityMetaDataOptionalByEntityId(String entityId, List<EntityMetaData>
        entityMetaDatas) {
        return entityMetaDatas.stream().filter(sp -> sp.getEntityId().equals(entityId)).collect
            (singletonOptionalCollector());
    }

    @Override
    public EntityMetaData serviceProviderByEntityId(String entityId) {
        return entityMetaData(entityId, serviceProviderOptionalByEntityId(entityId));
    }

    @Override
    public EntityMetaData identityProviderByEntityId(String entityId) {
        return entityMetaData(entityId, identityProviderOptionalByEntityId(entityId));
    }

    @Override
    public List<String> identityProviderNames(List<String> entityIds) {
        return identityProviders().stream().filter(idp -> entityIds.contains(idp.getEntityId())).map
            (EntityMetaData::getNameEn).collect(toList());
    }

    private EntityMetaData entityMetaData(String entityId, Optional<EntityMetaData> entityMetaDataOptional) {
        if (!entityMetaDataOptional.isPresent()) {
            LOG.error(entityId + " is not a valid or known IdP / SP entityId");
            throw new PolicyIdpAccessUnknownIdentityProvidersException(entityId + " is not a valid or known IdP / SP " +
                "entityId");
        }
        return entityMetaDataOptional.get();
    }

    @SuppressWarnings("unchecked")
    protected List<EntityMetaData> parseEntities(Resource resource) {
        try {
            List<Map<String, Object>> list = objectMapper.readValue(resource.getInputStream(), List.class);

            return list.stream().map(entry -> {
                Map<String, Object> data = (Map<String, Object>) entry.get("data");
                Map<String, Object> metaDataFields = (Map<String, Object>) data.get("metaDataFields");
                return
                        new EntityMetaData(
                            (String) data.get("entityid"),
                            (String) metaDataFields.get("coin:institution_id"),
                            (String) metaDataFields.get("name:en"),
                            (String) metaDataFields.get("name:nl"),
                            getPolicyEnforcementDecisionRequired(metaDataFields),
                            getAllowedAll(data),
                            getAllowedEntries(data)
                        );
                }
            ).sorted(sortEntityMetaData()).collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> getAllowedEntries(Map<String, Object> entry) {
        List<String> allowedEntities = (List<String>) entry.getOrDefault("allowedEntities", Collections.emptyList());
        return new HashSet<>(allowedEntities);
    }

    private boolean getAllowedAll(Map<String, Object> entry) {
        return (boolean) entry.getOrDefault("allowedall", true);
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
        return Comparator.comparing(this::getEntityMetaDataComparatorId);
    }

    private String getEntityMetaDataComparatorId(EntityMetaData metaData) {
        return metaData.getNameEn() != null ? metaData.getNameEn() : metaData.getNameNl() != null ? metaData
            .getNameNl() : metaData.getEntityId();
    }

    /**
     * Not part of the Manage contract, but used for testing
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
