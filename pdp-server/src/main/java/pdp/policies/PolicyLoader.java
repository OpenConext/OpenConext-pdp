package pdp.policies;

import pdp.domain.PdpPolicy;

import java.util.List;

public interface PolicyLoader {

  String authenticatingAuthority = "htpp://mock-idp";
  String userIdentifier = "system";
  String userDisplayName = "system";

  List<PdpPolicy> getPolicies();

  void loadPolicies();
}
