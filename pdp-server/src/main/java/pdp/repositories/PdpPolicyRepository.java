package pdp.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pdp.domain.PdpPolicy;

public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {

  Long countByName(@Param("name") String name);
}
