package pdp.xacml;

import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.pdp.policy.FunctionDefinition;
import org.apache.openaz.xacml.pdp.std.StdFunctionDefinitionFactory;
import org.springframework.util.ReflectionUtils;
import pdp.ip.IPRangeFunctionDefinition;
import pdp.negate.NegateFunctionDefinition;

import java.lang.reflect.Field;
import java.util.Map;


public class CustomFunctionDefinitionFactory extends StdFunctionDefinitionFactory {

    @SuppressWarnings("unchecked")
    public CustomFunctionDefinitionFactory() {
        super();
        /**
         * The StdFunctionDefinitionFactory was unfortunately not designed for extensibility
         */
        Field field = ReflectionUtils.findField(StdFunctionDefinitionFactory.class, "mapFunctionDefinitions");
        ReflectionUtils.makeAccessible(field);
        Map<Identifier, FunctionDefinition> mapFunctionDefinitions =
            (Map<Identifier, FunctionDefinition>) ReflectionUtils.getField(field, this);

        IPRangeFunctionDefinition ipRangeFunctionDefinition = new IPRangeFunctionDefinition();
        NegateFunctionDefinition negateFunctionDefinition = new NegateFunctionDefinition();

        mapFunctionDefinitions.put(ipRangeFunctionDefinition.getId(), ipRangeFunctionDefinition);
        mapFunctionDefinitions.put(negateFunctionDefinition.getId(), negateFunctionDefinition);
    }
}
