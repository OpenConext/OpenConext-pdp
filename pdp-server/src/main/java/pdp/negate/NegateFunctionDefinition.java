package pdp.negate;

import org.apache.openaz.xacml.api.AttributeValue;
import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.api.XACML;
import org.apache.openaz.xacml.pdp.eval.EvaluationContext;
import org.apache.openaz.xacml.pdp.policy.Bag;
import org.apache.openaz.xacml.pdp.policy.ExpressionResult;
import org.apache.openaz.xacml.pdp.policy.FunctionArgument;
import org.apache.openaz.xacml.pdp.policy.FunctionDefinition;
import org.apache.openaz.xacml.std.IdentifierImpl;
import org.apache.openaz.xacml.std.StdStatus;
import org.apache.openaz.xacml.std.StdStatusCode;
import org.apache.openaz.xacml.std.datatypes.DataTypeBoolean;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static pdp.xacml.PdpPolicyDefinitionParser.NEGATE_FUNCTION;

public class NegateFunctionDefinition implements FunctionDefinition {

    private static final IdentifierImpl IDENTIFIER =
            new IdentifierImpl(NEGATE_FUNCTION);

    @Override
    public Identifier getId() {
        return IDENTIFIER;
    }

    @Override
    public Identifier getDataTypeId() {
        return XACML.ID_DATATYPE_BOOLEAN;
    }

    @Override
    public boolean returnsBag() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExpressionResult evaluate(EvaluationContext evaluationContext, List<FunctionArgument> arguments) {
        if (arguments.size() != 2) {
            return ExpressionResult.newError(new StdStatus(StdStatusCode.STATUS_CODE_SYNTAX_ERROR));
        }
        String policyValue = (String) arguments.get(0).getValue().getValue();

        FunctionArgument functionArgument = arguments.get(1);
        Bag bag = functionArgument.getBag();
        List<String> allValues;
        if (bag != null) {
            Iterator<AttributeValue<?>> attributeValuesIterator = bag.getAttributeValues();
            List<AttributeValue<String>> attributeValues = new ArrayList<>();
            attributeValuesIterator.forEachRemaining(attributeValue -> attributeValues.add((AttributeValue<String>) attributeValue));
            allValues = attributeValues.stream()
                    .filter(Objects::nonNull)
                    .map(AttributeValue::getValue)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        } else {
            String value = (String) functionArgument.getValue().getValue();
            allValues =  StringUtils.hasText(value) ? List.of(value) : Collections.emptyList();
        }
        boolean anyMatch = allValues.stream().anyMatch(value -> value.equalsIgnoreCase(policyValue));
        AttributeValue<Boolean> booleanAttributeValue = anyMatch ? DataTypeBoolean.AV_FALSE : DataTypeBoolean.AV_TRUE;

        return ExpressionResult.newSingle(booleanAttributeValue);
    }
}
