package pdp.repositories;

import org.springframework.data.repository.CrudRepository;
import pdp.domain.PdpDecision;

import java.util.Date;
import java.util.List;

public interface PdpDecisionRepository extends CrudRepository<PdpDecision, Long> {

  List<PdpDecision> findByCreatedAfter(Date date);

}
