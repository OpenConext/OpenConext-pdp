package pdp.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pdp.domain.PdpPolicyViolation;

import java.util.List;

public interface PdpPolicyViolationRepository extends CrudRepository<PdpPolicyViolation, Long> {

  List<PdpPolicyViolation> findByAssociatedAdviceId(@Param("associatedAdviceId") String associatedAdviceId);

  Long countByAssociatedAdviceId(@Param("associatedAdviceId") String associatedAdviceId);
}
