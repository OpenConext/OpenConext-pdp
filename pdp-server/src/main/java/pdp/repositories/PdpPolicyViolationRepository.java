package pdp.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pdp.domain.PdpPolicyViolation;

import java.util.List;

public interface PdpPolicyViolationRepository extends CrudRepository<PdpPolicyViolation, Long> {

  String NO_POLICY_ID = "no_policy_id";

  List<PdpPolicyViolation> findByPolicyId(@Param("policyId") String policyId);

  @Query("select p.policyId, count(p.id) from pdp.domain.PdpPolicyViolation p group by p.policyId")
  List<Object[]> findCountPerPolicyId();

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM pdp_policy_violations WHERE created < (NOW() - INTERVAL :retentionDays DAY)", nativeQuery = true)
  int deleteOlderThenRetentionDays(@Param("retentionDays") int retentionDays);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM pdp_policy_violations WHERE policy_id = :policyId", nativeQuery = true)
  int deleteByPolicyId(@Param("policyId") String policyId);

}
