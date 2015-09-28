package pdp.domain;

public class EntityMetaData {

  private final String entityId;
  private final String description;

  public EntityMetaData(String entityId, String description) {
    this.entityId = entityId;
    this.description = description;
  }

  public String getEntityId() {
    return entityId;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return "EntityMetaData{" +
        "entityId='" + entityId + '\'' +
        ", description='" + description + '\'' +
        '}';
  }
}
