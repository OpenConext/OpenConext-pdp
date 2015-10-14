package pdp.xacml;

import org.springframework.core.io.Resource;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;

import java.util.List;

public interface PolicyLoader {

  List<PdpPolicy> getPolicies();

  void loadPolicies();
}
