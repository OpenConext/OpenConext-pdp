package pdp.ip;

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

public class IPRangeFunctionDefinition implements FunctionDefinition {

    private static final IdentifierImpl IDENTIFIER =
        new IdentifierImpl("urn:surfnet:cbac:custom:function:3.0:ip:range");

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
        String cidr = String.class.cast(arguments.get(0).getValue().getValue());
        String ipAddress = String.class.cast(arguments.get(1).getValue().getValue());

        IpAddressMatcher matcher = new IpAddressMatcher(cidr);
        AttributeValue attributeValue;
        try {
            attributeValue = matcher.matches(ipAddress) ? DataTypeBoolean.AV_TRUE : DataTypeBoolean.AV_FALSE;
        } catch (IllegalArgumentException e) {
            return ExpressionResult.newError(new StdStatus(StdStatusCode.STATUS_CODE_PROCESSING_ERROR,
                "Invalid IP address: ".concat(ipAddress)));
        }
        return ExpressionResult.newSingle(attributeValue);
    }
}
