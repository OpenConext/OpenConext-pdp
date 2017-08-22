package pdp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import pdp.domain.PdpDecision;

import java.util.Date;
import java.util.List;

public interface PdpDecisionRepository extends PagingAndSortingRepository<PdpDecision, Long> {

    List<PdpDecision> findByCreatedAfter(Date date);

}
