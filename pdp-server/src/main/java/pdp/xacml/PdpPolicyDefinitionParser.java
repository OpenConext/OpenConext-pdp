package pdp.xacml;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeDesignator;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeValueExpression;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.domain.PdpAttribute;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static pdp.util.StreamUtils.iteratorToList;
import static pdp.util.StreamUtils.singletonCollector;

/*
 * Thread-safe
 */
public class PdpPolicyDefinitionParser {

  private static final Logger LOG = LoggerFactory.getLogger(PdpPolicyDefinitionParser.class);

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
    definition.setActive(pdpPolicy.isActive());

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
    Rule denyRule = getRule(rules, Decision.DENY);

    parseAdviceExpression(definition, denyRule);

    if (!definition.isDenyRule()) {
      return;
    }
    parseAttributes(definition, denyRule, Decision.DENY);
  }

  private void parsePermit(String policyXml, PdpPolicyDefinition definition, List<Rule> rules) {
    if (definition.isDenyRule()) {
      return;
    }
    Rule permitRule = getRule(rules, Decision.PERMIT);
    parseAttributes(definition, permitRule, Decision.PERMIT);
  }

  private boolean isPermitRule(String policyXml, List<Rule> rules) {
    Rule rule = getRule(rules, Decision.PERMIT);
    return rule.getTarget().getAnyOfs() != null;
  }

  private Rule getRule(List<Rule> rules, Decision decision) {
    return rules.stream().filter(r -> r.getRuleEffect().getDecision().equals(decision)).collect(singletonCollector());
  }

  private void parseAttributes(PdpPolicyDefinition definition, Rule rule, Decision decision) {
    List<AnyOf> anyOfs = iteratorToList(rule.getTarget().getAnyOfs());
    if (anyOfs.size() > 1) {
      definition.setAllAttributesMustMatch(true);
    }
    List<AllOf> allOfs = anyOfs.stream().map(anyOf -> iteratorToList(anyOf.getAllOfs())).flatMap(allOf -> allOf.stream()).collect(toList());
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
    targetAnyOfs.forEach(anyOf -> {
      List<AllOf> targetAllOfs = iteratorToList(anyOf.getAllOfs());
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

  private void parseAdviceExpression(PdpPolicyDefinition definition, Rule denyRule) {
    AdviceExpression adviceExpression = iteratorToList(denyRule.getAdviceExpressions()).stream().collect(singletonCollector());

    List<AttributeAssignmentExpression> attributeAssignmentExpressions = iteratorToList(adviceExpression.getAttributeAssignmentExpressions());

    String denyMesssageEN = extractDenyMessage(attributeAssignmentExpressions, "en");
    String denyMesssageNL = extractDenyMessage(attributeAssignmentExpressions, "nl");
    definition.setDenyAdvice(denyMesssageEN);
    definition.setDenyAdviceNl(denyMesssageNL);
  }

  private String extractDenyMessage(List<AttributeAssignmentExpression> attributeAssignmentExpressions, String language) {
    return attributeAssignmentExpressions.stream()
        .filter(ase -> ase.getAttributeId().getUri().toString().equals("DenyMessage:" + language))
        .map(ase -> (String) (((AttributeValueExpression) ase.getExpression()).getAttributeValue().getValue()))
        .collect(singletonCollector());
  }

  public Policy parsePolicy(String policyXml) {
    String cleanedXml = policyXml.trim().replaceAll("\n", "").replaceAll(" +", " ");
    try {
      return (Policy) DOMPolicyDef.load(new ByteArrayInputStream(cleanedXml.getBytes()));
    } catch (DOMStructureException e) {
      LOG.warn("Failed to parse policyXml", e);
      throw new RuntimeException(e);
    }
  }

}
