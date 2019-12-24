package pdp.manage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import pdp.JsonMapper;
import pdp.access.PolicyIdpAccessUnknownIdentityProvidersException;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicyDefinition;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public interface Manage extends JsonMapper {

    Logger LOG = LoggerFactory.getLogger(Manage.class);

    List<EntityMetaData> serviceProviders();

    List<EntityMetaData> identityProviders();

    Set<EntityMetaData> identityProvidersByAuthenticatingAuthority(String authenticatingAuthority);

    Set<EntityMetaData> serviceProvidersByInstitutionId(String institutionId);

    Optional<EntityMetaData> serviceProviderOptionalByEntityId(String entityId);

    Optional<EntityMetaData> identityProviderOptionalByEntityId(String entityId);

    Map<String, EntityMetaData> identityProvidersByEntityIds(Collection<String> entityIds);

    Map<String, EntityMetaData> serviceProvidersByEntityIds(Collection<String> entityIds);

    EntityMetaData serviceProviderByEntityId(String entityId);

    EntityMetaData identityProviderByEntityId(String entityId);

    @SuppressWarnings("unchecked")
    default List<EntityMetaData> parseEntities(Resource resource) {
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
                            (boolean) data.getOrDefault("allowedall", true),
                            new HashSet<>((List<String>) data.getOrDefault("allowedEntities", Collections.emptyList()))
                        );
                }
            ).sorted(Comparator.comparing(metaData -> metaData.getNameEn() != null ? metaData.getNameEn() : metaData.getNameNl() != null ? metaData
                .getNameNl() : metaData.getEntityId())).collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default boolean getPolicyEnforcementDecisionRequired(Map<String, Object> entry) {
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

    default EntityMetaData nonEmptyOptionalToEntityMetaData(String entityId, Optional<EntityMetaData> entityMetaDataOptional) {
        if (!entityMetaDataOptional.isPresent()) {
            LOG.error(entityId + " is not a valid or known IdP / SP entityId");
            throw new PolicyIdpAccessUnknownIdentityProvidersException(entityId + " is not a valid or known IdP / SP " +
                "entityId");
        }
        return entityMetaDataOptional.get();
    }

}
