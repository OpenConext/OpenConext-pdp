package pdp.repositories;

import org.springframework.data.repository.CrudRepository;
import pdp.domain.PdpMigratedPolicy;

public interface PdpMigratedPolicyRepository extends CrudRepository<PdpMigratedPolicy, Long> {
}
