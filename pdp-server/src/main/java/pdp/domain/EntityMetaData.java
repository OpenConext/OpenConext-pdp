package pdp.domain;

import java.io.Serializable;
import java.util.Set;

import static java.util.Arrays.stream;

public class EntityMetaData implements Serializable {

    private final String entityId;
    private final String institutionId;
    private final String descriptionEn;
    private final String nameEn;
    private final String descriptionNl;
    private final String nameNl;
    private final boolean policyEnforcementDecisionRequired;
    private boolean allowedAll;
    private final Set<String> allowedEntityIds;

    public EntityMetaData(String entityId, String institutionId, String descriptionEn, String nameEn, String descriptionNl,
                          String nameNl, boolean policyEnforcementDecisionRequired, boolean allowedAll, Set<String> allowedEntityIds) {
        this.entityId = entityId;
        this.institutionId = institutionId;
        this.descriptionEn = descriptionEn;
        this.nameEn = nameEn;
        this.descriptionNl = descriptionNl;
        this.nameNl = nameNl;
        this.policyEnforcementDecisionRequired = policyEnforcementDecisionRequired;
        this.allowedAll = allowedAll;
        this.allowedEntityIds = allowedEntityIds;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getDescriptionNl() {
        return descriptionNl;
    }

    public String getNameNl() {
        return nameNl;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public boolean isPolicyEnforcementDecisionRequired() {
        return policyEnforcementDecisionRequired;
    }

    public boolean isAllowedFrom(String... entityIds) {
        return allowedAll || stream(entityIds).anyMatch(entityId -> allowedEntityIds.contains(entityId));
    }


}
