package pdp.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pdp.domain.PdpPolicyViolation;

import java.util.List;

public interface PdpPolicyViolationRepository extends CrudRepository<PdpPolicyViolation, Long> {

  @Query("SELECT p.policy.id, COUNT(p.id) FROM pdp.domain.PdpPolicyViolation p GROUP BY p.policy")
  List<Object[]> findCountPerPolicyId();

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM pdp_policy_violations WHERE created < (NOW() - INTERVAL :retentionDays DAY)", nativeQuery = true)
  int deleteOlderThenRetentionDays(@Param("retentionDays") int retentionDays);

}
