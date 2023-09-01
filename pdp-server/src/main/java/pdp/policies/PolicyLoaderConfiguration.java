package pdp.policies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import pdp.PolicyViolationRetentionPeriodCleaner;
import pdp.mail.MailBox;
import pdp.manage.Manage;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;

@Configuration
public class PolicyLoaderConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    @Autowired
    @Profile({"dev", "no-csrf"})
    public PolicyLoader developmentPrePolicyLoader(@Value("${policy.base.dir}") String policyBaseDir, PdpPolicyRepository pdpPolicyRepository, PdpPolicyViolationRepository pdpPolicyViolationRepository) {
        return new DevelopmentPrePolicyLoader(resourceLoader.getResource(policyBaseDir), pdpPolicyRepository, pdpPolicyViolationRepository);
    }

    @Bean
    @Autowired
    @Profile({"perf"})
    public PolicyLoader performancePrePolicyLoader(@Value("${performance.pre.policy.loader.count}") int count, Manage
            manage, PdpPolicyRepository pdpPolicyRepository, PdpPolicyViolationRepository pdpPolicyViolationRepository) {
        return new PerformancePrePolicyLoader(count, manage, pdpPolicyRepository, pdpPolicyViolationRepository);
    }

    @Bean
    @Autowired
    @Profile({"local", "test", "acc", "prod", "mail"})
    public PolicyLoader noopPolicyLoader() {
        return new NoopPrePolicyLoader();
    }

    @Bean
    public PolicyViolationRetentionPeriodCleaner policyViolationRetentionPeriodCleaner(@Value("${policy.violation.retention.period.days}") int retentionPeriodDays,
                                                                                       PdpPolicyViolationRepository pdpPolicyViolationRepository,
                                                                                       @Value("${pdpCronJobResponsible}") boolean pdpCronJobResponsible) {
        return new PolicyViolationRetentionPeriodCleaner(retentionPeriodDays, pdpPolicyViolationRepository, pdpCronJobResponsible);
    }

    @Bean
    public PolicyMissingServiceProviderValidator policyMissingServiceProviderValidator(MailBox mailBox,
                                                                                       Manage manage,
                                                                                       PdpPolicyRepository pdpPolicyRepository,
                                                                                       @Value("${pdpCronJobResponsible}") boolean pdpCronJobResponsible) {
        return new PolicyMissingServiceProviderValidator(mailBox, manage, pdpPolicyRepository, pdpCronJobResponsible);
    }


}
