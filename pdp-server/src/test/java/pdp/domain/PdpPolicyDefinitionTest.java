package pdp.domain;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import pdp.JsonMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PdpPolicyDefinitionTest implements JsonMapper {

    @Test
    public void testEquals() throws Exception {
        PdpPolicyDefinition def1 = getPdpPolicyDefinition();
        PdpPolicyDefinition def2 = getPdpPolicyDefinition();

        assertEquals(def1, def2);

        assertEquals(1, new HashSet<>(Arrays.asList(def1, def2)).size());

        String s = def1.toString();

        assertTrue(s.contains("test_policy"));
    }

    @Test
    public void testIgnoreDate() throws IOException {
        PdpPolicyDefinition policyDefinition = getPdpPolicyDefinition();
        policyDefinition.setCreated(new Date());

        String json = objectMapper.writeValueAsString(policyDefinition);
        assertTrue(json.contains("created"));

        PdpPolicyDefinition definition = objectMapper.readValue(json, PdpPolicyDefinition.class);
        Date created = definition.getCreated();
        assertNull(created);
    }

    private PdpPolicyDefinition getPdpPolicyDefinition() throws java.io.IOException {
        return objectMapper.readValue(IOUtils.toString(new ClassPathResource("xacml/json-policies/policy_definition.json").getInputStream()), PdpPolicyDefinition.class);
    }


}