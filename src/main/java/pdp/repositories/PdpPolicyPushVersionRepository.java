package pdp.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pdp.domain.PdpPolicyPushVersion;

public interface PdpPolicyPushVersionRepository extends CrudRepository<PdpPolicyPushVersion, Long> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE pdp_policy_push_version SET version = version + 1 WHERE id = 1", nativeQuery = true)
    void incrementVersion();

    @Query(value = "SELECT version FROM pdp_policy_push_version WHERE id = 1", nativeQuery = true)
    Long getCurrentVersion();

}
