package pdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pdp.repositories.PdpPolicyViolationRepository;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

@Component
public class PolicyViolationRetentionPeriodCleaner {

  private final static Logger LOG = LoggerFactory.getLogger(PolicyViolationRetentionPeriodCleaner.class);
  private final PdpPolicyViolationRepository pdpPolicyViolationRepository;
  private final int retentionPeriodDays;

  @Autowired
  public PolicyViolationRetentionPeriodCleaner(@Value("${policy.violation.retention.period.days}") int retentionPeriodDays,
                                               PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
    this.retentionPeriodDays = retentionPeriodDays;
    newScheduledThreadPool(1).scheduleAtFixedRate(this::clean, 0, 1, TimeUnit.DAYS);
  }

  private void clean() {
    LOG.info("Start deleting policy violations older then {} days. Current # of policy violations is {}", this.retentionPeriodDays, this.pdpPolicyViolationRepository.count());
    int deleted = this.pdpPolicyViolationRepository.deleteOlderThenRetentionDays(this.retentionPeriodDays);
    LOG.info("Finished deleting policy violations older then {} days. Deleted {} policy violations", this.retentionPeriodDays, deleted);
  }
}
