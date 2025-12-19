package pdp.bag;

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

import static pdp.xacml.PdpPolicyDefinitionParser.BAG_FUNCTION;
import static pdp.xacml.PdpPolicyDefinitionParser.NEGATE_FUNCTION;

public class BagFunctionDefinition implements FunctionDefinition {

    private static final IdentifierImpl IDENTIFIER =
            new IdentifierImpl(BAG_FUNCTION);

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
        String actualValue = (String) arguments.get(0).getValue().getValue();

        List<String> policyValues = arguments.subList(1, arguments.size()).stream()
            .map(arg -> (String) arg.getValue().getValue())
            .toList();

        boolean anyMatch = policyValues.contains(actualValue);
        AttributeValue<Boolean> booleanAttributeValue = anyMatch ? DataTypeBoolean.AV_TRUE : DataTypeBoolean.AV_FALSE;

        return ExpressionResult.newSingle(booleanAttributeValue);
    }
}
