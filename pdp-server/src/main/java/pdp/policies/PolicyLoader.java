package pdp.policies;

import org.springframework.core.io.Resource;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;

import java.util.List;

public interface PolicyLoader {

  String authenticatingAuthority = "htpp://mock-idp";
  String userIdentifier = "system";
  String userDisplayName = "system";

  List<PdpPolicy> getPolicies();

  void loadPolicies();
}
