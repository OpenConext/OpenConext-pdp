package pdp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.CollectionUtils;
import pdp.xacml.PolicyTemplateEngine;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

public class PdpPolicyDefinition {

  private Long id;

  @NotNull
  @Size(min = 1)
  private String name;

  @NotNull
  @Size(min = 1)
  private String description;

  @NotNull
  @Size(min = 1)
  private String serviceProviderId;
  private String serviceProviderName;
  private boolean serviceProviderInvalidOrMissing; 

  private List<String> identityProviderIds = new ArrayList<>();
  private List<String> identityProviderNames = new ArrayList<>();

  @Valid
  private List<PdpAttribute> attributes = new ArrayList<>();

  @NotNull
  @Size(min = 1)
  private String denyAdvice;

  private boolean denyRule;

  private boolean allAttributesMustMatch;

  private Date created;

  private String userDisplayName;

  private String authenticatingAuthorityName;

  private int numberOfViolations;

  private int numberOfRevisions;

  @NotNull
  @Size(min = 1)
  private String denyAdviceNl;

  private int revisionNbr;

  private boolean isActivatedSr;

  private boolean active;

  private boolean actionsAllowed;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public List<String> getIdentityProviderIds() {
    return identityProviderIds;
  }

  public void setIdentityProviderIds(List<String> identityProviderIds) {
    this.identityProviderIds = identityProviderIds;
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

  public boolean isDenyRule() {
    return denyRule;
  }

  public void setDenyRule(boolean denyRule) {
    this.denyRule = denyRule;
  }

  public boolean isAllAttributesMustMatch() {
    return allAttributesMustMatch;
  }

  public void setAllAttributesMustMatch(boolean allAttributesMustMatch) {
    this.allAttributesMustMatch = allAttributesMustMatch;
  }

  public String getPolicyId() {
    return PolicyTemplateEngine.getPolicyId(name);
  }

  public String getServiceProviderName() {
    return serviceProviderName;
  }

  public void setServiceProviderName(String serviceProviderName) {
    this.serviceProviderName = serviceProviderName;
  }

  public boolean isServiceProviderInvalidOrMissing() {
	return serviceProviderInvalidOrMissing;
  }

  public void setServiceProviderInvalidOrMissing(boolean serviceProviderInvalidOrMissing) {
	this.serviceProviderInvalidOrMissing = serviceProviderInvalidOrMissing;
  }
  
  public List<String> getIdentityProviderNames() {
    return identityProviderNames;
  }

  public void setIdentityProviderNames(List<String> identityProviderNames) {
    this.identityProviderNames = identityProviderNames;
  }

  public int getNumberOfViolations() {
    return numberOfViolations;
  }

  public void setNumberOfViolations(int numberOfViolations) {
    this.numberOfViolations = numberOfViolations;
  }

  public void setDenyAdviceNl(String denyAdviceNl) {
    this.denyAdviceNl = denyAdviceNl;
  }

  public String getDenyAdviceNl() {
    return denyAdviceNl;
  }

  public int getNumberOfRevisions() {
    return numberOfRevisions;
  }

  public void setNumberOfRevisions(int numberOfRevisions) {
    this.numberOfRevisions = numberOfRevisions;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getUserDisplayName() {
    return userDisplayName;
  }

  public void setUserDisplayName(String userDisplayName) {
    this.userDisplayName = userDisplayName;
  }

  public void setRevisionNbr(int revisionNbr) {
    this.revisionNbr = revisionNbr;
  }

  public int getRevisionNbr() {
    return revisionNbr;
  }

  public boolean isActivatedSr() {
    return isActivatedSr;
  }

  public void setActivatedSr(boolean activatedSr) {
    isActivatedSr = activatedSr;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isActionsAllowed() {
    return actionsAllowed;
  }

  public void setActionsAllowed(boolean actionsAllowed) {
    this.actionsAllowed = actionsAllowed;
  }

  public String getAuthenticatingAuthorityName() {
    return authenticatingAuthorityName;
  }

  public void setAuthenticatingAuthorityName(String authenticatingAuthorityName) {
    this.authenticatingAuthorityName = authenticatingAuthorityName;
  }

  //used in the mustache templates
  @JsonIgnore
  public List<String> anyIdentityProviders() {
    return CollectionUtils.isEmpty(this.identityProviderIds) ? Collections.emptyList() : Arrays.asList("will-iterate-once");
  }

  @JsonIgnore
  public Set<Map.Entry<String, List<PdpAttribute>>> allAttributesGrouped() {
    return this.attributes.stream().collect(groupingBy(PdpAttribute::getName)).entrySet();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PdpPolicyDefinition that = (PdpPolicyDefinition) o;
    return Objects.equals(denyRule, that.denyRule) &&
        Objects.equals(allAttributesMustMatch, that.allAttributesMustMatch) &&
        Objects.equals(name, that.name) &&
        Objects.equals(description, that.description) &&
        Objects.equals(serviceProviderId, that.serviceProviderId) &&
        Objects.equals(identityProviderIds, that.identityProviderIds) &&
        Objects.equals(attributes, that.attributes) &&
        Objects.equals(denyAdvice, that.denyAdvice) &&
        Objects.equals(denyAdviceNl, that.denyAdviceNl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, serviceProviderId, identityProviderIds, attributes, denyAdvice, denyRule, allAttributesMustMatch);
  }

  @Override
  public String toString() {
    return "PdpPolicyDefinition{" + "\n" +
        "name='" + name + '\'' + "\n" +
        ", description='" + description + '\'' + "\n" +
        ", serviceProviderId='" + serviceProviderId + '\'' + "\n" +
        ", identityProviderIds=" + identityProviderIds + "\n" +
        ", attributes=" + attributes + "\n" +
        ", denyAdvice='" + denyAdvice + '\'' + "\n" +
        ", denyAdviceNl='" + denyAdviceNl + '\'' + "\n" +
        ", denyRule=" + denyRule + "\n" +
        ", allAttributesMustMatch=" + allAttributesMustMatch + "\n" +
        '}';
  }

  public static PdpPolicyDefinition policyDefinition(String serviceProviderId, List<String> identityProvidersIds) {
    PdpPolicyDefinition definition = new PdpPolicyDefinition();
    definition.setServiceProviderId(serviceProviderId);
    definition.setServiceProviderName(serviceProviderId);
    definition.setIdentityProviderIds(identityProvidersIds);
    return definition;
  }

}
