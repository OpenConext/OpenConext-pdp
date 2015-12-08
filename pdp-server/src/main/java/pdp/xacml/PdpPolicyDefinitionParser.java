package pdp.xacml;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeDesignator;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeValueExpression;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.springframework.util.CollectionUtils;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;

import java.io.ByteArrayInputStream;
import java.util.*;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static pdp.PdpApplication.singletonOptionalCollector;

/*
 * Thread-safe
 */
public class PdpPolicyDefinitionParser {

  public static final String SP_ENTITY_ID = "SPentityID";

  public static final String IDP_ENTITY_ID = "IDPentityID";

  public static final String NAME_ID = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";

  public PdpPolicyDefinition parse(PdpPolicy pdpPolicy) {
    PdpPolicyDefinition definition = new PdpPolicyDefinition();
    String policyXml = pdpPolicy.getPolicyXml();

    Policy policy = parsePolicy(policyXml);

    definition.setId(pdpPolicy.getId());
    definition.setName(pdpPolicy.getName());
    definition.setDescription(policy.getDescription());
    definition.setCreated(pdpPolicy.getCreated());
    definition.setUserDisplayName(pdpPolicy.getUserDisplayName());
    definition.setRevisionNbr(pdpPolicy.getRevisionNbr());

    parseTargets(policyXml, definition, policy);

    List<Rule> rules = iteratorToList(policy.getRules());
    definition.setDenyRule(!isPermitRule(policyXml, rules));

    parsePermit(policyXml, definition, rules);
    parseDeny(policyXml, definition, rules);

    //we need to sort to get a consistent attribute list for testing - run-time it makes no difference
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

  private void parsePermit(String policyXml, PdpPolicyDefinition definition, List<Rule> rules) {
    if (definition.isDenyRule()) {
      return;
    }
    Rule permitRule = getRule(policyXml, rules, Decision.PERMIT);
    parseAttributes(policyXml, definition, permitRule, Decision.PERMIT);
  }

  private boolean isPermitRule(String policyXml, List<Rule> rules) {
    Rule rule = getRule(policyXml, rules, Decision.PERMIT);
    return rule.getTarget().getAnyOfs() != null;
  }

  private Rule getRule(String policyXml, List<Rule> rules, Decision decision) {
    Optional<Rule> rule = rules.stream().filter(r -> r.getRuleEffect().getDecision().equals(decision)).findFirst();
    if (!rule.isPresent()) {
      throw new PdpParseException("No " + decision + " rule defined in the Policy " + policyXml);
    }
    return rule.get();
  }

  private void parseAttributes(String policyXml, PdpPolicyDefinition definition, Rule rule, Decision decision) {
    List<AnyOf> anyOfs = iteratorToList(rule.getTarget().getAnyOfs());
    if (CollectionUtils.isEmpty(anyOfs)) {
      throw new PdpParseException("Expected at least 1 anyOf in the " + decision + " rule for " + decision + " policy " + policyXml);
    }
    if (anyOfs.size() > 1) {
      definition.setAllAttributesMustMatch(true);
    }
    List<AllOf> allOfs = anyOfs.stream().map(anyOf -> iteratorToList(anyOf.getAllOfs())).flatMap(allOf -> allOf.stream()).collect(toList());
    if (CollectionUtils.isEmpty(allOfs)) {
      throw new PdpParseException("Expected at least one allOf in the " + decision + " rule for " + decision + " policy " + policyXml);
    }
    List<Match> matches = allOfs.stream().map(allOf -> iteratorToList(allOf.getMatches())).flatMap(m -> m.stream()).collect(toList());
    List<PdpAttribute> pdpAttributes = matches.stream().map(match -> {
      String attributeName = ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString();
      String attributeValue = (String) match.getAttributeValue().getValue();
      return new PdpAttribute(attributeName, attributeValue);
    }).collect(toList());
    definition.setAttributes(pdpAttributes);
  }

  private void parseTargets(String policyXml, PdpPolicyDefinition definition, Policy policy) {
    List<AnyOf> targetAnyOfs = iteratorToList(policy.getTarget().getAnyOfs());
    if (CollectionUtils.isEmpty(targetAnyOfs) || targetAnyOfs.size() > 2) {
      throw new PdpParseException("Expected 2 and only two anyOf in the Target section " + policyXml);
    }
    targetAnyOfs.forEach(anyOf -> {
      List<AllOf> targetAllOfs = iteratorToList(anyOf.getAllOfs());
      if (CollectionUtils.isEmpty(targetAllOfs)) {
        throw new PdpParseException("Expected at least 1 allOfs in the Target anyOf sections " + policyXml);
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
      throw new PdpParseException("SPentityID is required " + policyXml);
    }
  }

  private void parseAdviceExpression(String policyXml, PdpPolicyDefinition definition, Rule denyRule) {
    List<AdviceExpression> adviceExpressions = iteratorToList(denyRule.getAdviceExpressions());
    if (CollectionUtils.isEmpty(adviceExpressions) || adviceExpressions.size() > 1) {
      throw new PdpParseException("Expected 1 and only one adviceExpressions in the Deny rule " + policyXml);
    }
    AdviceExpression adviceExpression = adviceExpressions.get(0);

    List<AttributeAssignmentExpression> attributeAssignmentExpressions = iteratorToList(adviceExpression.getAttributeAssignmentExpressions());
    if (CollectionUtils.isEmpty(attributeAssignmentExpressions) || attributeAssignmentExpressions.size() != 2) {
      throw new PdpParseException("Expected 2 and only two attributeAssignmentExpressions in the Deny rule " + policyXml);
    }
    String denyMesssageEN = extractDenyMessage(policyXml, attributeAssignmentExpressions, "en");
    String denyMesssageNL = extractDenyMessage(policyXml, attributeAssignmentExpressions, "nl");
    definition.setDenyAdvice(denyMesssageEN);
    definition.setDenyAdviceNl(denyMesssageNL);
  }

  private String extractDenyMessage(String policyXml, List<AttributeAssignmentExpression> attributeAssignmentExpressions, String language) {
    Optional<String> denyMessage = attributeAssignmentExpressions.stream().filter(ase -> ase.getAttributeId().getUri().toString().equals("DenyMessage:" + language)).map(ase -> (String) (((AttributeValueExpression) ase.getExpression()).getAttributeValue().getValue())).collect(singletonOptionalCollector());
    if (!denyMessage.isPresent()) {
      throw new PdpParseException("Expected 1 and only one AttributeAssignmentExpression in the Deny rule with AttributeId " + "DenyMessage:" + language + "  " + policyXml);
    }
    return denyMessage.get();
  }

  public static final Policy parsePolicy(String policyXml) {
    String cleanedXml = policyXml.trim().replaceAll("\n", "").replaceAll(" +", " ");
    try {
      return (Policy) DOMPolicyDef.load(new ByteArrayInputStream(cleanedXml.getBytes()));
    } catch (DOMStructureException e) {
      throw new RuntimeException(e);
    }
  }

  private <E> List<E> iteratorToList(Iterator<E> iterator) {
    return stream(spliteratorUnknownSize(iterator, ORDERED), false).collect(toCollection(ArrayList::new));
  }

}
