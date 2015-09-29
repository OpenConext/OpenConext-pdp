package pdp.domain;

public class EntityMetaData {

  private final String entityId;
  private final String descriptionEn;
  private final String nameEn;
  private final String descriptionNl;
  private final String nameNl;

  public EntityMetaData(String entityId, String descriptionEn, String nameEn, String descriptionNl, String nameNl) {
    this.entityId = entityId;
    this.descriptionEn = descriptionEn;
    this.nameEn = nameEn;
    this.descriptionNl = descriptionNl;
    this.nameNl = nameNl;
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
}
