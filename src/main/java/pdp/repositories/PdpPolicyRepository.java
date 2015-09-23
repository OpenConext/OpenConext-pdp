package pdp.repositories;

import org.springframework.data.repository.CrudRepository;
import pdp.domain.PdpPolicy;

public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {
}
