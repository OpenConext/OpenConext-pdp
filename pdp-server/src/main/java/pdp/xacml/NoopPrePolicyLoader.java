package pdp.xacml;

import org.springframework.core.io.Resource;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;

import java.util.Collections;
import java.util.List;

public class NoopPrePolicyLoader implements PolicyLoader {

  @Override
  public List<PdpPolicy> getPolicies() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public void loadPolicies() {
    //noop
  }
}
