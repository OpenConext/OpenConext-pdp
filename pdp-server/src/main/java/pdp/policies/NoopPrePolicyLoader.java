package pdp.policies;

import pdp.domain.PdpPolicy;

import java.util.Collections;
import java.util.List;

public class NoopPrePolicyLoader implements PolicyLoader {

    @Override
    public List<PdpPolicy> getPolicies() {
        return Collections.emptyList();
    }

    @Override
    public void loadPolicies() {
        //noop
    }
}
