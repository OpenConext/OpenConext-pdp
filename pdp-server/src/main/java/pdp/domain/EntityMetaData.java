package pdp.domain;

import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Set;

import static java.util.Arrays.stream;

public class EntityMetaData implements Serializable {

    private final String entityId;
    private final String institutionId;
    private final String nameEn;
    private final String nameNl;
    private final String organisationNameEn;
    private final String organisationNameNl;
    private final boolean policyEnforcementDecisionRequired;
    private final boolean allowedAll;
    private final Set<String> allowedEntityIds;

    public EntityMetaData(String entityId,
                          String institutionId,
                          String nameEn,
                          String nameNl,
                          String organisationNameEn,
                          String organisationNameNl,
                          boolean policyEnforcementDecisionRequired,
                          boolean allowedAll,
                          Set<String> allowedEntityIds) {
        this.entityId = entityId;
        this.institutionId = institutionId;
        this.nameEn = StringUtils.hasText(nameEn) ? nameEn : entityId;
        this.nameNl = StringUtils.hasText(nameNl) ? nameNl : entityId;
        this.organisationNameEn = StringUtils.hasText(organisationNameEn) ? organisationNameEn : "";
        this.organisationNameNl = StringUtils.hasText(organisationNameNl) ? organisationNameEn : this.organisationNameEn;
        this.policyEnforcementDecisionRequired = policyEnforcementDecisionRequired;
        this.allowedAll = allowedAll;
        this.allowedEntityIds = allowedEntityIds;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getNameEn() {
        return nameEn;
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
        return allowedAll || stream(entityIds).anyMatch(allowedEntityIds::contains);
    }

    public String getOrganisationNameEn() {
        return organisationNameEn;
    }

    public String getOrganisationNameNl() {
        return organisationNameNl;
    }
}
