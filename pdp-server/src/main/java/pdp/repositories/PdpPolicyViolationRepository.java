package pdp.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pdp.domain.PdpPolicyViolation;

import java.util.List;

public interface PdpPolicyViolationRepository extends CrudRepository<PdpPolicyViolation, Long> {

  String NO_POLICY_ID = "no_policy_id";

  List<PdpPolicyViolation> findByPolicyId(@Param("policyId") String policyId);

    Long countByPolicyId(@Param("policyId") String policyId);

  @Query("select p.policyId, count(p.id) from pdp.domain.PdpPolicyViolation p group by p.policyId")
  List<Object[]> findCountPerPolicyId();

}
