package pdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import pdp.repositories.PdpPolicyViolationRepository;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class PolicyViolationRetentionPeriodCleaner {

  private final static Logger LOG = LoggerFactory.getLogger(PolicyViolationRetentionPeriodCleaner.class);

  @Autowired
  public PolicyViolationRetentionPeriodCleaner(int retentionPeriodDays, PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    newScheduledThreadPool(1).scheduleAtFixedRate(() -> clean(retentionPeriodDays, pdpPolicyViolationRepository), 0, 1, TimeUnit.DAYS);
  }

  private void clean(int retentionPeriodDays, PdpPolicyViolationRepository pdpPolicyViolationRepository) {
    LOG.info("Start deleting policy violations older then {} days. Current # of policy violations is {}", retentionPeriodDays, pdpPolicyViolationRepository.count());
    int deleted = pdpPolicyViolationRepository.deleteOlderThenRetentionDays(retentionPeriodDays);
    LOG.info("Finished deleting policy violations older then {} days. Deleted {} policy violations", retentionPeriodDays, deleted);
  }
}
