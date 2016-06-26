package pdp.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pdp.domain.PdpDecision;
import pdp.repositories.PdpDecisionRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class StatsController {

  //http://stackoverflow.com/questions/2447324/streaming-large-result-sets-with-mysql

  @Autowired
  private PdpDecisionRepository pdpDecisionRepository;

  @RequestMapping(method = RequestMethod.GET, value = "/internal/decisions")
  public List<PdpDecision> decisions(@RequestParam("daysAgo") int daysAgo) {
    Date date = Date.from(Instant.now().minus(daysAgo, ChronoUnit.DAYS));
    return pdpDecisionRepository.findByCreatedAfter(date);
  }



}
