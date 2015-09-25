package pdp.xacml;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeDesignator;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeValueExpression;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.springframework.util.CollectionUtils;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicyDefinition;

import java.io.ByteArrayInputStream;
import java.util.*;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/*
 * Thread-safe
 */
public class PdpPolicyDefinitionParser {

  private static final String SP_ENTITY_ID = "SPentityID";

  private static final String IDP_ENTITY_ID = "IDPentityID";

  public PdpPolicyDefinition parse(String policyName, String policyXml) {
    PdpPolicyDefinition definition = new PdpPolicyDefinition();

    Policy policy = parsePolicy(policyXml);

    definition.setName(policyName);
    definition.setDescription(policy.getDescription());

    parseTargets(policyXml, definition, policy);

    List<Rule> rules = iteratorToList(policy.getRules());
    definition.setDenyRule(!isPermitRule(policyXml, rules));

    parsePermit(policyXml, definition, rules);
    parseDeny(policyXml, definition, rules);

    Collections.sort(definition.getAttributes(), (a1, a2) -> a1.getName().compareTo(a2.getName()));

    return definition;
  }

  private void parseDeny(String policyXml, PdpPolicyDefinition definition, List<Rule> rules) {
    Rule denyRule = getRule(policyXml, rules, Decision.DENY);

    parseAdviceExpression(policyXml, definition, denyRule);

    if (!definition.isDenyRule()) {
      return;
    }
    parseAttributes(policyXml, definition, denyRule, Decision.DENY);
  }

  private void parseAttributes(String policyXml, PdpPolicyDefinition definition, Rule rule, Decision decision) {
    List<AnyOf> anyOfs = iteratorToList(rule.getTarget().getAnyOfs());
    if (CollectionUtils.isEmpty(anyOfs)) {
      throw new RuntimeException("Expected at least 1 anyOf in the " + decision + " rule for " + decision + " policy " + policyXml);
    }
    List<AllOf> allOfs = anyOfs.stream().map(anyOf -> iteratorToList(anyOf.getAllOfs())).flatMap(allOf -> allOf.stream()).collect(toList());
    if (CollectionUtils.isEmpty(allOfs)) {
      throw new RuntimeException("Expected at least one allOf in the " + decision + " rule for " + decision + " policy " + policyXml);
    }
    List<Match> matches = allOfs.stream().map(allOf -> iteratorToList(allOf.getMatches())).flatMap(m -> m.stream()).collect(toList());
    List<PdpAttribute> pdpAttributes = matches.stream().map(match -> {
      String attributeName = ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString();
      String attributeValue = (String) match.getAttributeValue().getValue();
      return new PdpAttribute(attributeName, attributeValue);
    }).collect(toList());
    definition.setAttributes(pdpAttributes);
  }


  private void parsePermit(String policyXml, PdpPolicyDefinition definition, List<Rule> rules) {
    if (definition.isDenyRule()) {
      return;
    }
    Rule permitRule = getRule(policyXml, rules, Decision.PERMIT);
    parseAttributes(policyXml, definition, permitRule, Decision.PERMIT);
  }

  private boolean isPermitRule(String policyXml, List<Rule> rules) {
    Rule permitRule = getRule(policyXml, rules, Decision.PERMIT);
    return permitRule.getTarget().getAnyOfs() != null;
  }

  private Rule getRule(String policyXml, List<Rule> rules, Decision decision) {
    Optional<Rule> permitRule = rules.stream().filter(rule -> rule.getRuleEffect().getDecision().equals(decision)).findFirst();
    if (!permitRule.isPresent()) {
      throw new RuntimeException("No " + decision + " rule defined in the Policy " + policyXml);
    }
    return permitRule.get();
  }

  private void parseTargets(String policyXml, PdpPolicyDefinition definition, Policy policy) {
    List<AnyOf> targetAnyOfs = iteratorToList(policy.getTarget().getAnyOfs());
    if (CollectionUtils.isEmpty(targetAnyOfs) || targetAnyOfs.size() > 2) {
      throw new RuntimeException("Expected 2 and only two anyOf in the Target section " + policyXml);
    }
    targetAnyOfs.forEach(anyOf -> {
      List<AllOf> targetAllOfs = iteratorToList(anyOf.getAllOfs());
      if (CollectionUtils.isEmpty(targetAllOfs)) {
        throw new RuntimeException("Expected at least 1 allOfs in the Target anyOf sections " + policyXml);
      }
      List<Match> targetMatches = targetAllOfs.stream().map(allOf -> iteratorToList(allOf.getMatches())).flatMap(Collection::stream).collect(toList());
      Optional<Match> spEntityID = targetMatches.stream().filter(match -> ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString().equalsIgnoreCase(SP_ENTITY_ID)).findFirst();
      if (spEntityID.isPresent()) {
        definition.setServiceProviderId((String) spEntityID.get().getAttributeValue().getValue());
      }
      List<String> idpEntityIDs = targetMatches.stream().filter(match ->
          ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString().equalsIgnoreCase(IDP_ENTITY_ID))
          .map(match -> (String) match.getAttributeValue().getValue()).collect(toList());
      definition.setIdentityProviderIds(idpEntityIDs);
    });

    if (definition.getServiceProviderId() == null) {
      throw new RuntimeException("SPentityID is required " + policyXml);
    }
  }

  private void parseAdviceExpression(String policyXml, PdpPolicyDefinition definition, Rule denyRule) {
    List<AdviceExpression> adviceExpressions = iteratorToList(denyRule.getAdviceExpressions());
    if (CollectionUtils.isEmpty(adviceExpressions) || adviceExpressions.size() > 1) {
      throw new RuntimeException("Expected 1 and only one adviceExpressions in the Deny rule " + policyXml);
    }
    AdviceExpression adviceExpression = adviceExpressions.get(0);

    List<AttributeAssignmentExpression> attributeAssignmentExpressions = iteratorToList(adviceExpression.getAttributeAssignmentExpressions());
    if (CollectionUtils.isEmpty(attributeAssignmentExpressions) || attributeAssignmentExpressions.size() > 1) {
      throw new RuntimeException("Expected 1 and only one attributeAssignmentExpressions in the Deny rule " + policyXml);
    }
    String denyAttributeValue = (String) ((AttributeValueExpression) attributeAssignmentExpressions.get(0).getExpression()).getAttributeValue().getValue();
    definition.setDenyAdvice(denyAttributeValue);
  }

  private Policy parsePolicy(String policyXml) {
    Policy policy;
    try {
      policy = (Policy) DOMPolicyDef.load(new ByteArrayInputStream(policyXml.replaceFirst("\n", "").getBytes()));
    } catch (DOMStructureException e) {
      throw new RuntimeException(e);
    }
    return policy;
  }

  private <E> List<E> iteratorToList(Iterator<E> iterator) {
    return stream(spliteratorUnknownSize(iterator, ORDERED), false).collect(toCollection(ArrayList::new));
  }
}
