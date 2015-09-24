package pdp.domain;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeDesignator;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeValueExpression;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class PdpPolicyDefintion {

  private String name;
  private String description;
  private String serviceProviderId;
  private String identityProviderId;
  private List<PdpAttribute> attributes = new ArrayList<>();
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

  public PdpPolicyDefintion(String policyName, String policyXml) throws DOMStructureException {
    this.setName(policyName);

    Policy policy = (Policy) DOMPolicyDef.load(new ByteArrayInputStream(policyXml.replaceFirst("\n", "").getBytes()));
    this.setDescription(policy.getDescription());

    List<AnyOf> targetAnyOfs = iteratorToList(policy.getTarget().getAnyOfs());
    if (StringUtils.isEmpty(targetAnyOfs) || targetAnyOfs.size() > 1) {
      throw new RuntimeException("Expected 1 and only one anyOf in the Target section "+ policyXml);
    }
    List<AllOf> targetAllOfs = iteratorToList(targetAnyOfs.get(0).getAllOfs());
    if (StringUtils.isEmpty(targetAllOfs) || targetAllOfs.size() > 1) {
      throw new RuntimeException("Expected 1 and only one allOfs in the Target anyOf section "+ policyXml);
    }
    List<Match> targetMatches = iteratorToList(targetAllOfs.get(0).getMatches());
    if (StringUtils.isEmpty(targetMatches) || targetMatches.size() > 2) {
      throw new RuntimeException("Only 1 or 2 matches allowed in the Target anyOf section "+ policyXml);
    }
    Optional<Match> spEntityID = targetMatches.stream().filter(match -> ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString().equalsIgnoreCase("SPentityID")).findFirst();
    if (!spEntityID.isPresent()) {
      throw new RuntimeException("SPentityID is required "+ policyXml);
    }
    this.setServiceProviderId((String) spEntityID.get().getAttributeValue().getValue());

    Optional<Match> idpEntityID = targetMatches.stream().filter(match -> ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString().equalsIgnoreCase("IDPentityID")).findFirst();
    if (idpEntityID.isPresent()) {
      this.setIdentityProviderId((String) idpEntityID.get().getAttributeValue().getValue());
    }

    List<Rule> rules = iteratorToList(policy.getRules());

    //Permit
    Optional<Rule> permitRule = rules.stream().filter(rule -> rule.getRuleEffect().getDecision().equals(Decision.PERMIT)).findFirst();
    if (!permitRule.isPresent()) {
      throw new RuntimeException("No Permit rule defined in the Policy "+ policyXml);
    }
    List<AnyOf> anyOfsPermit = iteratorToList(permitRule.get().getTarget().getAnyOfs());
    if (StringUtils.isEmpty(anyOfsPermit) || anyOfsPermit.size() > 1) {
      throw new RuntimeException("Expected 1 and only one anyOf in the Permit rule "+ policyXml);
    }
    List<AllOf> allOfsPermit = iteratorToList(anyOfsPermit.get(0).getAllOfs());
    if (StringUtils.isEmpty(allOfsPermit)) {
      throw new RuntimeException("Expected at least one allOf in the Permit rule "+ policyXml);
    }
    List<Match> permitMatches = allOfsPermit.stream().map(allOf -> iteratorToList(allOf.getMatches())).flatMap(matches -> matches.stream()).collect(toList());
    List<PdpAttribute> pdpAttributes = permitMatches.stream().map(match -> {
      String attributeName = ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString();
      String attributeValue = (String) match.getAttributeValue().getValue();
      return new PdpAttribute(attributeName, attributeValue);
    }).collect(toList());
    this.setAttributes(pdpAttributes);

    //Deny
    Optional<Rule> denyRule = rules.stream().filter(rule -> rule.getRuleEffect().getDecision().equals(Decision.DENY)).findFirst();
    if (!denyRule.isPresent()) {
      throw new RuntimeException("No Deny rule defined in the Policy "+ policyXml);
    }
    List<AdviceExpression> adviceExpressions = iteratorToList(denyRule.get().getAdviceExpressions());
    if (StringUtils.isEmpty(adviceExpressions) || adviceExpressions.size() > 1) {
      throw new RuntimeException("Expected 1 and only one adviceExpressions in the Deny rule "+ policyXml);
    }
    AdviceExpression adviceExpression = adviceExpressions.get(0);

    List<AttributeAssignmentExpression> attributeAssignmentExpressions = iteratorToList(adviceExpression.getAttributeAssignmentExpressions());
    if (StringUtils.isEmpty(attributeAssignmentExpressions) || attributeAssignmentExpressions.size() > 1) {
      throw new RuntimeException("Expected 1 and only one attributeAssignmentExpressions in the Deny rule "+ policyXml);
    }
    String denyAttributeValue = (String) ((AttributeValueExpression)attributeAssignmentExpressions.get(0).getExpression()).getAttributeValue().getValue();
    this.setDenyAdvice(denyAttributeValue);

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

  private <E> List<E> iteratorToList(Iterator<E> iterator) {
    return stream(spliteratorUnknownSize(iterator, ORDERED), false).collect(toCollection(ArrayList::new));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PdpPolicyDefintion defintion = (PdpPolicyDefintion) o;
    return Objects.equals(name, defintion.name) &&
        Objects.equals(description, defintion.description) &&
        Objects.equals(serviceProviderId, defintion.serviceProviderId) &&
        Objects.equals(identityProviderId, defintion.identityProviderId) &&
        Objects.equals(attributes, defintion.attributes) &&
        Objects.equals(denyAdvice, defintion.denyAdvice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, serviceProviderId, identityProviderId, attributes, denyAdvice);
  }

  @Override
  public String toString() {
    return "PdpPolicyDefintion{" +
        "name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", serviceProviderId='" + serviceProviderId + '\'' +
        ", identityProviderId='" + identityProviderId + '\'' +
        ", attributes=" + attributes +
        ", denyAdvice='" + denyAdvice + '\'' +
        '}';
  }
}
