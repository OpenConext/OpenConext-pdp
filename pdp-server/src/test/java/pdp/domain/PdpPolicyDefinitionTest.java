package pdp.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PdpPolicyDefinitionTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testEquals() throws Exception {
    PdpPolicyDefinition def1 = getPdpPolicyDefinition();
    PdpPolicyDefinition def2 = getPdpPolicyDefinition();

    assertEquals(def1, def2);

    assertEquals(1, new HashSet<>(Arrays.asList(def1, def2)).size());

    String s = def1.toString();

    assertTrue(s.contains("test_policy"));
  }

  private PdpPolicyDefinition getPdpPolicyDefinition() throws java.io.IOException {
    return objectMapper.readValue(IOUtils.toString(new ClassPathResource("xacml/json-policies/policy_definition.json").getInputStream()), PdpPolicyDefinition.class);
  }
}