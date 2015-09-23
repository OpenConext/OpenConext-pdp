package pdp.domain;

import java.util.List;

public class PdpPolicyDefintion {

  private String name;
  private String description;
  private String serviceProviderId;
  private String identityProviderId;
  private List<PdpAttribute> attributes;
  private String denyAdvice;

  public PdpPolicyDefintion() {
  }

  public PdpPolicyDefintion(String name, String description, String serviceProviderId, String identityProviderId, List<PdpAttribute> attributes, String denyAdvice) {
    this.name = name;
    this.description = description;
    this.serviceProviderId = serviceProviderId;
    this.identityProviderId = identityProviderId;
    this.attributes = attributes;
    this.denyAdvice = denyAdvice;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getServiceProviderId() {
    return serviceProviderId;
  }

  public void setServiceProviderId(String serviceProviderId) {
    this.serviceProviderId = serviceProviderId;
  }

  public String getIdentityProviderId() {
    return identityProviderId;
  }

  public void setIdentityProviderId(String identityProviderId) {
    this.identityProviderId = identityProviderId;
  }

  public List<PdpAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<PdpAttribute> attributes) {
    this.attributes = attributes;
  }

  public String getDenyAdvice() {
    return denyAdvice;
  }

  public void setDenyAdvice(String denyAdvice) {
    this.denyAdvice = denyAdvice;
  }

  public String getNameId() {
    return name.replace(" ","_").toLowerCase();
  }

}
