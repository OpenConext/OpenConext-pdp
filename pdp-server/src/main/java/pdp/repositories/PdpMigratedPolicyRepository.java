package pdp.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pdp.domain.PdpMigratedPolicy;

public interface PdpMigratedPolicyRepository extends CrudRepository<PdpMigratedPolicy, Long> {

    @Modifying(flushAutomatically = true)
    @Query(value = "delete from pdp_migrated_policies", nativeQuery = true)
    void deleteAllFlush();
}
