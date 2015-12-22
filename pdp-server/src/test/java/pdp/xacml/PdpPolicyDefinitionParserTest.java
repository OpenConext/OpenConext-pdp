package pdp.xacml;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import pdp.domain.PdpPolicy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.fail;

public class PdpPolicyDefinitionParserTest {

  private PdpPolicyDefinitionParser subject = new PdpPolicyDefinitionParser();

  @Test
  public void testParse() throws Exception {
    //happy path is extensively tested in the StandAlonePdpEngineTest
    //we test here the various constraints
    List<PdpPolicy> policies = Arrays.asList("no.advice.deny.xml", "no.nl.advice.xml", "no.any.xml", "no.assignment.deny.xml", "no.sp.xml")
        .stream().map(name -> getPolicy(name)).collect(toList());
    policies.forEach(policy -> {
      try {
        subject.parse(policy);
        fail();
      } catch (PdpParseException p) {
      }
    });
  }

  private PdpPolicy getPolicy(String name) {
    PdpPolicy policy = new PdpPolicy();
    //called form lambda
    try {
      String xml = IOUtils.toString(new ClassPathResource("xacml/invalid-policies/" + name).getInputStream());
      policy.setPolicyXml(xml);
      policy.setName(name);
      return policy;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}