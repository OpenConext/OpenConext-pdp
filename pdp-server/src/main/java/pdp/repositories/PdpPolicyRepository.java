package pdp.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pdp.domain.PdpPolicy;

import java.util.List;
import java.util.stream.Stream;

public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {

  List<PdpPolicy> findFirstByPolicyId(@Param("policyId") String policyId);

}
