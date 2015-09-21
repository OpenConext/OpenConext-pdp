package pdp.xacml;

import org.apache.openaz.xacml.pdp.policy.Policy;
import org.apache.openaz.xacml.pdp.policy.PolicyDef;
import org.apache.openaz.xacml.pdp.policy.PolicyFinder;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.std.StdEvaluationContextFactory;
import org.apache.openaz.xacml.pdp.std.StdPolicyFinder;
import org.apache.openaz.xacml.std.StdStatusCode;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.PdpPolicyRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.StreamSupport.stream;

public class RepositoryEvaluationContextFactory extends StdEvaluationContextFactory {

  private static Logger LOG = LoggerFactory.getLogger(RepositoryEvaluationContextFactory.class);

  private PdpPolicyRepository pdpPolicyRepository;
  private boolean cachePolicies;

  public RepositoryEvaluationContextFactory() throws IOException {
    this.cachePolicies = Boolean.valueOf(XACMLProperties.getProperties().getProperty("openconext.pdp.cachePolicies", "true"));
  }

  public void setPdpPolicyRepository(PdpPolicyRepository pdpPolicyRepository) {
    this.pdpPolicyRepository = pdpPolicyRepository;
    setPolicyFinder(loadPolicyFinder());
  }

  @Override
  protected PolicyFinder getPolicyFinder() {
    if (cachePolicies) {
      return super.getPolicyFinder();
    } else {
      return loadPolicyFinder();
    }
  }

  private PolicyFinder loadPolicyFinder() {
    Collection<PolicyDef> rootPolicies =
        stream(pdpPolicyRepository.findAll().spliterator(), false).map(policy -> convertToPolicyDef(policy.getPolicyXml())).collect(toCollection(ArrayList::new));
    LOG.info("(Re)-loaded {} policies from the database", rootPolicies.size());
    return new StdPolicyFinder(rootPolicies, null);
  }


  private PolicyDef convertToPolicyDef(String policyXml) {
    try {
      return DOMPolicyDef.load(new ByteArrayInputStream(policyXml.replaceFirst("\n", "").getBytes()));
    } catch (DOMStructureException e) {
      LOG.error("Error loading policy from " + policyXml, e);
      return new Policy(StdStatusCode.STATUS_CODE_SYNTAX_ERROR, e.getMessage());
    }
  }

}
