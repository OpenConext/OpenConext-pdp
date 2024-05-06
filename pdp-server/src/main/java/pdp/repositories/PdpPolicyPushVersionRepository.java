package pdp.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pdp.domain.PdpPolicyPushVersion;
import pdp.domain.PdpPolicyViolation;

import java.util.List;

public interface PdpPolicyPushVersionRepository extends CrudRepository<PdpPolicyPushVersion, Long> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE pdp_policy_push_version SET version = version + 1", nativeQuery = true)
    void incrementVersion();

}
