package pdp.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pdp.JsonMapper;
import pdp.domain.PdpDecision;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpDecisionRepository;
import pdp.repositories.PdpPolicyRepository;
import pdp.stats.StatsContext;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class StatsController implements JsonMapper {

    @Autowired
    private PdpDecisionRepository pdpDecisionRepository;

    @Autowired
    private PdpPolicyRepository pdpPolicyRepository;

    @GetMapping("/internal/decisions")
    public List<PdpDecision> decisions(@RequestParam("daysAgo") int daysAgo) {
        Date date = Date.from(Instant.now().minus(daysAgo, ChronoUnit.DAYS));
        return pdpDecisionRepository.findByCreatedAfter(date);
    }

    @GetMapping("internal/loas-stats")
    public Map<String, Map<String, Integer>> loas() {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        int page = 0;
        int pageSize = 1000;
        Pageable pageable = new PageRequest(page, pageSize, Sort.Direction.ASC, "id");
        Page<PdpDecision> decisionPage = pdpDecisionRepository.findAll(pageable);
        do {
            decisionPage.getContent().forEach(decision -> {
                StatsContext statsContext = statsContext(decision);
                String loa = statsContext.getLoa();
                String policyId = statsContext.getPolicyId();
                Optional<PdpPolicy> policyOptional = pdpPolicyRepository.findFirstByPolicyIdAndLatestRevision(policyId, true);
                String policyName = policyOptional.map(policy -> policy.getName()).orElse(policyId);
                if (loa != null && policyName != null) {
                    Map<String, Integer> loas = result.getOrDefault(policyName, new HashMap<>());
                    Integer count = loas.getOrDefault(loa, 0);
                    loas.put(loa, count + 1);
                    result.put(policyName, loas);
                }
            });
            pageable = new PageRequest(++page, pageSize, Sort.Direction.ASC, "id");
            decisionPage = pdpDecisionRepository.findAll(pageable);
        } while (decisionPage.getNumberOfElements() > 0);
        return result;
    }

    private StatsContext statsContext(PdpDecision decision) {
        try {
            return objectMapper.readValue(decision.getDecisionJson(), StatsContext.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
