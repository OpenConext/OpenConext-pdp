package pdp;

import org.apache.openaz.xacml.api.Advice;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.Result;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.policy.Policy;
import org.apache.openaz.xacml.pdp.policy.PolicyDef;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.std.StdStatusCode;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyViolation;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;
import pdp.xacml.PDPEngineHolder;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.apache.openaz.xacml.api.Decision.DENY;

@RestController
public class PdpController {

  private static Logger LOG = LoggerFactory.getLogger(PdpController.class);
  private final PDPEngineHolder pdpEngineHolder;
  private PDPEngine pdpEngine;
  private PdpPolicyViolationRepository pdpPolicyViolationRepository;
  private PdpPolicyRepository pdpPolicyRepository;
  private ReadWriteLock lock = new ReentrantReadWriteLock();

  @Autowired
  public PdpController(@Value("${initial.delay.policies.refresh.minutes}") int initialDelay,
                       @Value("${period.policies.refresh.minutes}") int period,
                       PdpPolicyViolationRepository pdpPolicyViolationRepository,
                       PdpPolicyRepository pdpPolicyRepository,
                       PDPEngineHolder pdpEngineHolder) {
    this.pdpEngineHolder = pdpEngineHolder;
    this.pdpEngine = pdpEngineHolder.newPdpEngine();
    this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
    this.pdpPolicyRepository = pdpPolicyRepository;

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable task = () -> this.refreshPolicies();
    executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MINUTES);
  }

  @RequestMapping(method = RequestMethod.POST, headers = {"content-type=application/json"}, value = "/decide")
  public String decide(@RequestBody String payload) throws Exception {
    long start = System.currentTimeMillis();
    LOG.debug("decide request: {}", payload);

    Request pdpRequest = JSONRequest.load(payload);
    Response pdpResponse;
    try {
      lock.readLock().lock();
      pdpResponse = pdpEngine.decide(pdpRequest);
    } finally {
      lock.readLock().unlock();
    }
    String response = JSONResponse.toString(pdpResponse, LOG.isDebugEnabled());
    LOG.debug("decide response: {} took: {} ms", response, System.currentTimeMillis() - start);

    reportPolicyViolation(pdpResponse, payload);
    return response;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/api/policies", produces = {"application/json"})
  public Map<String, PolicyDef> compoundPolicies() throws DOMStructureException {
    Iterable<PdpPolicy> all = pdpPolicyRepository.findAll();
    return stream(all.spliterator(), false).collect(Collectors.toMap(PdpPolicy::getName, (policy) -> convertToPolicyDef(policy.getPolicyXml())));
  }

  private PolicyDef convertToPolicyDef(String policyXml) {
    try {
      return DOMPolicyDef.load(new ByteArrayInputStream(policyXml.replaceFirst("\n", "").getBytes()));
    } catch (DOMStructureException e) {
      LOG.error("Error loading policy from " + policyXml, e);
      return new Policy(StdStatusCode.STATUS_CODE_SYNTAX_ERROR, e.getMessage());
    }
  }


  private void reportPolicyViolation(Response pdpResponse, String payload) {
    Collection<Result> results = pdpResponse.getResults();
    if (!CollectionUtils.isEmpty(results) && results.stream().anyMatch(result -> result.getDecision().equals(DENY))) {
      Collection<Advice> associatedAdvices = results.iterator().next().getAssociatedAdvice();
      String associatedAdviceId = CollectionUtils.isEmpty(associatedAdvices) ?
          "No associated advice present on Policy. Please check all policies and repair those without Deny advice" : associatedAdvices.iterator().next().getId().stringValue();
      pdpPolicyViolationRepository.save(new PdpPolicyViolation(associatedAdviceId, payload));
    }
  }


  private void refreshPolicies() {
    LOG.info("Starting reloading policies");
    long start = System.currentTimeMillis();
    lock.writeLock().lock();
    try {
      this.pdpEngine = pdpEngineHolder.newPdpEngine();
    } finally {
      lock.writeLock().unlock();
    }
    LOG.info("Finished reloading policies in {} ms", System.currentTimeMillis() - start);
  }

}
