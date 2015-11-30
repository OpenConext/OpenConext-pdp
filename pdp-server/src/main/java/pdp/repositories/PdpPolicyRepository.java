package pdp.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pdp.domain.PdpPolicy;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.Query;
public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {

  List<PdpPolicy> findFirstByPolicyId(@Param("policyId") String policyId);

  @Override
  @Query(value = "SELECT * FROM pdp_policies need_this WHERE need_this.latest_revision IS NULL", nativeQuery = true)
  Iterable<PdpPolicy> findAll();

  @Query(value = "SELECT p.id, (SELECT COUNT(*) FROM pdp_policies p2 WHERE p2.latest_revision = p.id) AS revision_count FROM pdp_policies p WHERE p.latest_revision IS NULL", nativeQuery = true)
  List<Object[]> findRevisionCountPerId();

}
