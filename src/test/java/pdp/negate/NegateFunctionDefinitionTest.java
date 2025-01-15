package pdp.negate;

import org.apache.openaz.xacml.pdp.policy.ExpressionResult;
import org.apache.openaz.xacml.pdp.policy.FunctionArgument;
import org.apache.openaz.xacml.pdp.policy.FunctionArgumentAttributeValue;
import org.apache.openaz.xacml.std.StdAttributeValue;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NegateFunctionDefinitionTest {

    NegateFunctionDefinition negateFunctionDefinition = new NegateFunctionDefinition();

    @Test
    public void testNegation() {
        ExpressionResult res = negateFunctionDefinition.evaluate(null, arguments("student", "student"));
        assertFalse((boolean) res.getValue().getValue());

        res = negateFunctionDefinition.evaluate(null, arguments("student", "teacher"));
        assertTrue((boolean) res.getValue().getValue());

        List<FunctionArgument> argumentList = arguments("student");
        argumentList.add(new FunctionArgumentAttributeValue(new StdAttributeValue<String>(null, null)));
        res = negateFunctionDefinition.evaluate(null, argumentList);
        assertTrue((boolean) res.getValue().getValue());

        res = negateFunctionDefinition.evaluate(null, arguments("student", ""));
        assertTrue((boolean) res.getValue().getValue());
    }

    private List<FunctionArgument> arguments(String... values) {
        return Stream.of(values)
                .map(val -> new FunctionArgumentAttributeValue(new StdAttributeValue<String>(null, val)))
                .collect(Collectors.toList());
    }

}