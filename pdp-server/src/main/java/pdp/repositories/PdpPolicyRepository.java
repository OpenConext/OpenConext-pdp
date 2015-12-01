package pdp.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pdp.domain.PdpPolicy;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.Query;
public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {

  List<PdpPolicy> findFirstByPolicyIdAndLatestRevision(String policyId, boolean latestRevision);

  @Override
  @Query(value = "SELECT * FROM pdp_policies p WHERE p.latest_revision = 1", nativeQuery = true)
  Iterable<PdpPolicy> findAll();

  @Query(value = "SELECT p.id, (SELECT COUNT(*) FROM pdp_policies p2 WHERE p2.revision_parent_id = p.id) AS revision_count FROM pdp_policies p WHERE p.revision_parent_id IS NULL", nativeQuery = true)
  List<Object[]> findRevisionCountPerId();

  @Override
  @Transactional
  @Modifying
  @Query(value = "DELETE FROM pdp_policies", nativeQuery = true)
  void deleteAll();
}
