package pdp.domain;

public class EntityMetaData {

  private final String entityId;
  private final String institutionId;
  private final String descriptionEn;
  private final String nameEn;
  private final String descriptionNl;
  private final String nameNl;
  private final boolean policyEnforcementDecisionRequired;

  public EntityMetaData(String entityId, String institutionId, String descriptionEn, String nameEn, String descriptionNl, String nameNl, boolean policyEnforcementDecisionRequired) {
    this.entityId = entityId;
    this.institutionId= institutionId;
    this.descriptionEn = descriptionEn;
    this.nameEn = nameEn;
    this.descriptionNl = descriptionNl;
    this.nameNl = nameNl;
    this.policyEnforcementDecisionRequired = policyEnforcementDecisionRequired;
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
}
