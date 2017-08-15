package pdp.ip;

import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.api.XACML;
import org.apache.openaz.xacml.pdp.eval.EvaluationContext;
import org.apache.openaz.xacml.pdp.policy.ExpressionResult;
import org.apache.openaz.xacml.pdp.policy.FunctionArgument;
import org.apache.openaz.xacml.pdp.policy.FunctionArgumentExpression;
import org.apache.openaz.xacml.pdp.policy.FunctionDefinition;
import org.apache.openaz.xacml.std.IdentifierImpl;
import org.apache.openaz.xacml.std.datatypes.DataTypeBoolean;

import java.util.List;

/**
 * https://stackoverflow.com/questions/17025046/java-library-to-check-if-ipv4-or-ipv6-address-is-in-a-given-subnet
 * https://github.com/edazdarevic/CIDRUtils/blob/master/CIDRUtils.java
 * https://stackoverflow.com/questions/35988060/how-to-calcuate-last-ip-address-from-cidr-in-ipv6
 *https://stackoverflow.com/questions/33784769/does-xacml-implement-a-not-equal-function
 *
 */
public class IPRangeFunctionDefinition implements FunctionDefinition {

    private static final IdentifierImpl IDENTIFIER = new IdentifierImpl("urn:surfnet:cbac:custom:function:3.0:ip:range");

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
            return ExpressionResult.newSingle(DataTypeBoolean.AV_TRUE);
        }
        FunctionArgument functionArgument = arguments.get(0);
        Object value = functionArgument.getValue().getValue();

        FunctionArgument f2 = arguments.get(1);
        if (f2 instanceof FunctionArgumentExpression) {
            FunctionArgumentExpression expression = (FunctionArgumentExpression) f2;
            Object value1 = f2.getValue().getValue();
            System.out.println(value1);
        }
        return ExpressionResult.newSingle(DataTypeBoolean.AV_TRUE);
    }
}
