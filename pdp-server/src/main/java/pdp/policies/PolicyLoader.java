package pdp.policies;

import pdp.domain.PdpPolicy;

import java.io.IOException;
import java.util.List;

public interface PolicyLoader {

    String authenticatingAuthority = "http://mock-idp";
    String userIdentifier = "system";
    String userDisplayName = "system";

    List<PdpPolicy> getPolicies() throws IOException;

    void loadPolicies() throws IOException;
}
