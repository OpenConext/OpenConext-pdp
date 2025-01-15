package pdp.xacml;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import pdp.domain.PdpPolicy;
import pdp.policies.DevelopmentPrePolicyLoader;
import pdp.policies.PolicyLoader;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class DevelopmentPrePolicyLoaderTest {

    private final DevelopmentPrePolicyLoader policyLoader = new DevelopmentPrePolicyLoader(new ClassPathResource("xacml/policies"), mock(PdpPolicyRepository.class), mock(PdpPolicyViolationRepository.class));

    private final Pattern policyIdPattern = Pattern.compile("PolicyId=\"(.*?)\"");

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