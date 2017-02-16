package pdp.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pdp.domain.PdpPolicy;

import java.util.List;
import java.util.Optional;

public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {

    Optional<PdpPolicy> findFirstByPolicyIdAndLatestRevision(String policyId, boolean latestRevision);

    Optional<PdpPolicy> findByNameAndLatestRevision(String name, boolean latestRevision);

    @Override
    @Query(value = "SELECT * FROM pdp_policies p WHERE p.latest_revision = 1", nativeQuery = true)
    Iterable<PdpPolicy> findAll();

    @Query(value = "SELECT p.id, (SELECT COUNT(*) FROM pdp_policies p2 WHERE p2.revision_parent_id = p.revision_parent_id) AS revision_count FROM pdp_policies p WHERE latest_revision = 1", nativeQuery = true)
    List<Object[]> findRevisionCountPerId();

    @Override
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM pdp_policies", nativeQuery = true)
    void deleteAll();
}
