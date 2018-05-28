package pdp.manage;

import com.google.common.collect.Sets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicyDefinition;

import java.util.Collections;
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

public class ClassPathResourceManage implements Manage {

    private Map<String, List<EntityMetaData>> entityMetaData = new ConcurrentHashMap<>();

    public ClassPathResourceManage() {
        initializeMetadata();
    }

    private void initializeMetadata() {
        Map<String, List<EntityMetaData>> newEntityMetaData = new ConcurrentHashMap<>();
        newEntityMetaData.put(IDP_ENTITY_ID, parseEntities(getIdpResource()));
        newEntityMetaData.put(SP_ENTITY_ID, parseEntities(getSpResource()));
        this.entityMetaData = newEntityMetaData;
        LOG.debug("Initialized Manage Resources. Number of IDPs {}. Number of SPs {}", entityMetaData.get(IDP_ENTITY_ID)
            .size(), entityMetaData.get(SP_ENTITY_ID).size());
    }

    private Resource getIdpResource() {
        return new ClassPathResource("manage/identity-providers.json");
    }

    private Resource getSpResource() {
        return new ClassPathResource("manage/service-providers.json");
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
        return nonEmptyOptionalToEntityMetaData(entityId, serviceProviderOptionalByEntityId(entityId));
    }

    @Override
    public EntityMetaData identityProviderByEntityId(String entityId) {
        return nonEmptyOptionalToEntityMetaData(entityId, identityProviderOptionalByEntityId(entityId));
    }

    @Override
    public void enrichPdPPolicyDefinition(PdpPolicyDefinition pd) {
        List<String> entityIds = pd.getIdentityProviderIds();
        List<EntityMetaData> metaDataList = identityProviders().stream().filter(idp -> entityIds.contains(idp.getEntityId
            ())).collect(toList());

        pd.setIdentityProviderNames(metaDataList.stream().map(EntityMetaData::getNameEn).collect(toList()));
        pd.setIdentityProviderNamesNl(metaDataList.stream().map(EntityMetaData::getNameNl).collect(toList()));
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
