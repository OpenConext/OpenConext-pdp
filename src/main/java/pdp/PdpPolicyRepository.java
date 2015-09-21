package pdp;

import org.springframework.data.repository.CrudRepository;
import pdp.PdpPolicy;

public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {
}
