package pdp.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pdp.domain.PdpPolicy;

import java.util.List;
import java.util.Optional;

public interface PdpPolicyRepository extends CrudRepository<PdpPolicy, Long> {

    @Override
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM pdp_policies", nativeQuery = true)
    void deleteAll();
}
