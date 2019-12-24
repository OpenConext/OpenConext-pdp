package pdp.domain;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import pdp.JsonMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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

    @Test
    public void sortLoas() throws IOException {
        PdpPolicyDefinition policyDefinition = getPdpPolicyDefinition();
        policyDefinition.getLoas().addAll(Arrays.asList(getLoa("level1"), getLoa("level2"), getLoa("level3")));
        policyDefinition.sortLoas();

        List<LoA> loas = policyDefinition.getLoas();
        assertEquals("level3", loas.get(0).getLevel());
        assertEquals("level2", loas.get(1).getLevel());
        assertEquals("level1", loas.get(2).getLevel());
    }

    private PdpPolicyDefinition getPdpPolicyDefinition() throws java.io.IOException {
        return objectMapper.readValue(IOUtils.toString(
                new ClassPathResource("xacml/json-policies/policy_definition.json").getInputStream(), "UTF-8"),
                PdpPolicyDefinition.class);
    }

    private LoA getLoa(String level) {
        LoA loA = new LoA();
        loA.setLevel(level);
        return loA;
    }


}