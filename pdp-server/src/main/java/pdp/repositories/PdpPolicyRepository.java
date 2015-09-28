package pdp.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pdp.domain.PdpPolicy;

import java.util.List;

public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {

    List<PdpPolicy> findByName(@Param("name") String name);

}
