package pdp.xacml;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;
import pdp.serviceregistry.ServiceRegistry;

import java.util.ArrayList;
import java.util.List;

public class PerformancePrePolicyLoader extends DevelopmentPrePolicyLoader {


  public PerformancePrePolicyLoader(ServiceRegistry serviceRegistry, PdpPolicyRepository pdpPolicyRepository) {
    super(new ByteArrayResource("noop".getBytes()), pdpPolicyRepository);
  }

  @Override
  public List<PdpPolicy> getPolicies() {
    // for every ServiceProvider create a policy
    return new ArrayList();
  }

}
