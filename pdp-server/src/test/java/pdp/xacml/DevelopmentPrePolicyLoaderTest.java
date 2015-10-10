package pdp.xacml;

import org.junit.Test;
import pdp.PolicyTemplateEngine;
import pdp.domain.PdpPolicy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class DevelopmentPrePolicyLoaderTest {

  private DevelopmentPrePolicyLoader policyLoader = new DevelopmentPrePolicyLoader();

  private Pattern policyIdPattern = Pattern.compile("PolicyId=\"(.*?)\"");

  @Test
  public void testGetPoliciesWithCorrectPolicyId() throws Exception {
    List<PdpPolicy> policies = policyLoader.getPolicies();
    long count = policies.stream().filter(p -> policyId(p).equals(PolicyTemplateEngine.getPolicyId(p.getName()))).count();
    assertEquals(count, policies.size());
  }

  private String policyId(PdpPolicy policy) {
    Matcher matcher = policyIdPattern.matcher(policy.getPolicyXml());
    assertEquals(true, matcher.find());
    return matcher.group(1);
  }
}