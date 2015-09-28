package pdp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeDesignator;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeValueExpression;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class PdpPolicyDefinition {

  @NotNull
  @Size(min = 1)
  private String name;

  @NotNull
  @Size(min = 1)
  private String description;

  @NotNull
  @Size(min = 1)
  private String serviceProviderId;

  private List<String> identityProviderIds = new ArrayList<>();

  @Valid
  private List<PdpAttribute> attributes = new ArrayList<>();

  @NotNull
  @Size(min = 1)
  private String denyAdvice;

  private boolean denyRule;

  private String denyId;

  public PdpPolicyDefinition() {
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

  public String getDenyId() {
    return denyId;
  }

  public void setDenyId(String denyId) {
    this.denyId = denyId;
  }

  public String getNameId() {
    return name.replace(" ", "_").toLowerCase();
  }

  @JsonIgnore
  public List anyIdentityProviders() {
    return CollectionUtils.isEmpty(this.identityProviderIds) ? Collections.EMPTY_LIST : Arrays.asList("will-iterate-once");
  }

  @JsonIgnore
  public Set<Map.Entry<String, List<PdpAttribute>>> allAttributesGrouped() {
    Set<Map.Entry<String, List<PdpAttribute>>> entries = this.attributes.stream().collect(Collectors.groupingBy(PdpAttribute::getName)).entrySet();
    return entries;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PdpPolicyDefinition defintion = (PdpPolicyDefinition) o;
    return Objects.equals(denyRule, defintion.denyRule) &&
        Objects.equals(name, defintion.name) &&
        Objects.equals(description, defintion.description) &&
        Objects.equals(serviceProviderId, defintion.serviceProviderId) &&
        Objects.equals(identityProviderIds, defintion.identityProviderIds) &&
        Objects.equals(attributes, defintion.attributes) &&
        Objects.equals(denyAdvice, defintion.denyAdvice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, serviceProviderId, identityProviderIds, attributes, denyAdvice, denyRule);
  }

  @Override
  public String toString() {
    return "PdpPolicyDefinition{" +
        "\n" + "name='" + name + '\'' +
        "\n" + ", description='" + description + '\'' +
        "\n" + ", serviceProviderId='" + serviceProviderId + '\'' +
        "\n" + ", identityProviderIds=" + identityProviderIds +
        "\n" + ", attributes=" + attributes +
        "\n" + ", denyAdvice='" + denyAdvice + '\'' +
        "\n" + ", denyRule=" + denyRule +
        "\n" + '}';
  }

}
