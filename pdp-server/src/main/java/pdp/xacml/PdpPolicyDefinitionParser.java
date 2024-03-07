package pdp.xacml;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.policy.dom.DOMApply;
import org.apache.openaz.xacml.pdp.policy.dom.DOMAttributeDesignator;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeDesignator;
import org.apache.openaz.xacml.pdp.policy.expressions.AttributeValueExpression;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import pdp.domain.*;
import pdp.web.IPAddressProvider;

import java.io.ByteArrayInputStream;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static pdp.util.StreamUtils.iteratorToList;
import static pdp.util.StreamUtils.singletonCollector;

/*
 * Thread-safe
 */
public class PdpPolicyDefinitionParser implements IPAddressProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PdpPolicyDefinitionParser.class);

    public static final String SP_ENTITY_ID = "SPentityID";
    public static final String IDP_ENTITY_ID = "IDPentityID";
    public static final String NAME_ID = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    public static final String IP_FUNCTION = "urn:surfnet:cbac:custom:function:3.0:ip:range";
    public static final String NEGATE_FUNCTION = "urn:surfnet:cbac:custom:function:3.0:negation";

    private static final String CLIENT_ID = "ClientID";

    public PdpPolicyDefinition parse(PdpPolicy pdpPolicy) {
        PdpPolicyDefinition definition = new PdpPolicyDefinition();
        String policyXml = pdpPolicy.getPolicyXml();

        Policy policy = parsePolicy(policyXml);

        Long id = pdpPolicy.getId();
        definition.setId(id != null ? id.toString() : null);
        definition.setName(pdpPolicy.getName());
        definition.setPolicyId(pdpPolicy.getPolicyId());
        definition.setDescription(policy.getDescription());
        definition.setCreated(pdpPolicy.getCreated());
        definition.setUserDisplayName(pdpPolicy.getUserDisplayName());
        definition.setRevisionNbr(pdpPolicy.getRevisionNbr());
        definition.setActive(pdpPolicy.isActive());
        definition.setType(pdpPolicy.getType());
        definition.setAuthenticatingAuthorityName(pdpPolicy.getAuthenticatingAuthority());

        parseTargets(policyXml, definition, policy);

        List<Rule> rules = iteratorToList(policy.getRules());

        if (pdpPolicy.getType().equals("step")) {
            definition.setDenyRule(false);
            List<LoA> loas = rules.stream()
                    .filter(rule -> rule.getObligationExpressions().hasNext())
                    .map(this::parseStepRule).collect(toList());
            definition.setLoas(loas);
            definition.sortLoas();
        } else {
            definition.setDenyRule(!isPermitRule(policyXml, rules));

            parsePermit(policyXml, definition, rules);
            parseDeny(policyXml, definition, rules);

            //we need to sort to get a consistent attribute list for testing - run-time it makes no difference
            Collections.sort(definition.getAttributes(), Comparator.comparing(PdpAttribute::getName));
        }

        return definition;
    }

    private LoA parseStepRule(Rule rule) {
        LoA loa = new LoA();

        AttributeValueExpression attributeValueExpression =
                AttributeValueExpression.class.cast(rule.getObligationExpressions().next()
                        .getAttributeAssignmentExpressions().next().getExpression());
        String level = (String) attributeValueExpression.getAttributeValue().getValue();
        loa.setLevel(level);

        Condition condition = rule.getCondition();
        if (condition != null) {
            DOMApply domApply = DOMApply.class.cast(condition.getExpression());
            boolean allAttributesMustMatch = domApply.getFunctionId().getUri().toString()
                    .endsWith("function:and");
            loa.setAllAttributesMustMatch(allAttributesMustMatch);
            this.parseArguments(loa, domApply.getArguments());
        }

        return loa;
    }

    private LoA parseArguments(LoA loA, Iterator<Expression> iterator) {
        List<Expression> expressions = iteratorToList(iterator);
        expressions.forEach(expression -> {
            if (expression instanceof DOMApply) {
                parseDomApply(loA, DOMApply.class.cast(expression));
            }
        });
        return loA;
    }

    private LoA parseDomApply(LoA loA, DOMApply domApply) {
        String functionID = domApply.getFunctionId().getUri().toString();
        if (functionID.endsWith("function:not")) {
            loA.setNegateCidrNotation(true);
            return parseArguments(loA, domApply.getArguments());
        } else if (functionID.equals(IP_FUNCTION)) {
            loA.getCidrNotations().add(parseCidrNotation(domApply));
            return loA;
        } else if (functionID.endsWith("function:string-is-in") || functionID.equals(NEGATE_FUNCTION)) {
            addArgumentToLoa(loA, domApply, functionID.equals(NEGATE_FUNCTION));
            return loA;
        }
        return parseArguments(loA, domApply.getArguments());
    }

    private void addArgumentToLoa(LoA loA, DOMApply domApply, boolean negated) {
        AttributeValueExpression attributeValueExpression = castArgument(AttributeValueExpression.class, domApply);
        String value = (String) attributeValueExpression.getAttributeValue().getValue();

        DOMAttributeDesignator attributeDesignator = castArgument(DOMAttributeDesignator.class, domApply);
        String name = attributeDesignator.getAttributeId().getUri().toString();

        PdpAttribute pdpAttribute = new PdpAttribute(name, value);
        pdpAttribute.setNegated(negated);
        loA.getAttributes().add(pdpAttribute);
    }

    private <T> T castArgument(Class<T> clazz, DOMApply domApply) {
        return clazz.cast(iteratorToList(domApply.getArguments())
                .stream()
                .filter(expression -> clazz.isAssignableFrom(expression.getClass()))
                .findFirst().get());
    }

    private CidrNotation parseCidrNotation(DOMApply ipRange) {
        String functionId = ipRange.getFunctionId().getUri().toString();
        if (!functionId.equals(IP_FUNCTION)) {
            throw new IllegalArgumentException("Expected IP_FUNCTION, but got " + functionId);
        }
        List<Expression> arguments = iteratorToList(ipRange.getArguments());
        Expression cidrNotationArgument = arguments.stream().filter(argument ->
                        argument instanceof AttributeValueExpression)
                .findFirst().get();
        String cidrNotation = (String) AttributeValueExpression.class.cast(cidrNotationArgument).getAttributeValue().getValue();
        String[] splitted = cidrNotation.split("/");
        return new CidrNotation(splitted[0], Integer.parseInt(splitted[1]),
                getIpInfo(splitted[0], Integer.parseInt(splitted[1])));
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

            List<String> spEntityIDs = targetMatches.stream().filter(match ->
                            ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString().equalsIgnoreCase(SP_ENTITY_ID))
                    .map(match -> (String) match.getAttributeValue().getValue()).collect(toList());
            if (!spEntityIDs.isEmpty()) {
                definition.setServiceProviderIds(spEntityIDs);
            }

            List<String> idpEntityIDs = targetMatches.stream().filter(match ->
                            ((AttributeDesignator) match.getAttributeRetrievalBase()).getAttributeId().getUri().toString().equalsIgnoreCase(IDP_ENTITY_ID))
                    .map(match -> (String) match.getAttributeValue().getValue()).collect(toList());
            if (!idpEntityIDs.isEmpty()) {
                definition.setIdentityProviderIds(idpEntityIDs);
            }

            Optional<Match> clientIdOptional = targetMatches.stream().filter(match -> ((AttributeDesignator) match.getAttributeRetrievalBase())
                    .getAttributeId().getUri().toString().equalsIgnoreCase(CLIENT_ID)).findFirst();
            clientIdOptional.ifPresent(clientId -> definition.setClientId((String) clientId.getAttributeValue().getValue()));
        });

        if (CollectionUtils.isEmpty(definition.getServiceProviderIds())) {
            throw new PdpParseException("SPentityIDs is required " + policyXml);
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
