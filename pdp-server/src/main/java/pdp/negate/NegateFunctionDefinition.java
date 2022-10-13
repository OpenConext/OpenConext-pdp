package pdp.negate;

import org.apache.openaz.xacml.api.AttributeValue;
import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.api.XACML;
import org.apache.openaz.xacml.pdp.eval.EvaluationContext;
import org.apache.openaz.xacml.pdp.policy.ExpressionResult;
import org.apache.openaz.xacml.pdp.policy.FunctionArgument;
import org.apache.openaz.xacml.pdp.policy.FunctionDefinition;
import org.apache.openaz.xacml.std.IdentifierImpl;
import org.apache.openaz.xacml.std.StdStatus;
import org.apache.openaz.xacml.std.StdStatusCode;
import org.apache.openaz.xacml.std.datatypes.DataTypeBoolean;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.List;

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
    public ExpressionResult evaluate(EvaluationContext evaluationContext, List<FunctionArgument> arguments) {
        if (arguments.size() != 2) {
            return ExpressionResult.newError(new StdStatus(StdStatusCode.STATUS_CODE_SYNTAX_ERROR));
        }
        String policyValue = (String) arguments.get(0).getValue().getValue();
        AttributeValue<String> value = (AttributeValue<String>) arguments.get(1).getValue();
        String actualValue = value != null ? value.getValue() : "";

        AttributeValue<Boolean> booleanAttributeValue = policyValue.equalsIgnoreCase(actualValue) ? DataTypeBoolean.AV_FALSE : DataTypeBoolean.AV_TRUE;

        return ExpressionResult.newSingle(booleanAttributeValue);
    }
}
