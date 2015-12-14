package pdp.xacml;

import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.pip.PIPFinder;
import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.std.StdEvaluationContextFactory;
import org.apache.openaz.xacml.pdp.std.StdPolicyFinder;
import org.apache.openaz.xacml.pdp.util.OpenAZPDPProperties;
import org.apache.openaz.xacml.std.IdentifierImpl;
import org.apache.openaz.xacml.std.StdVersion;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;
import pdp.sab.SabClient;
import pdp.teams.VootClient;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.StreamSupport.stream;
import static pdp.xacml.PolicyTemplateEngine.getPolicyId;

public class OpenConextEvaluationContextFactory extends StdEvaluationContextFactory {

  private static Logger LOG = LoggerFactory.getLogger(OpenConextEvaluationContextFactory.class);

  private PdpPolicyRepository pdpPolicyRepository;
  private boolean cachePolicies;
  private boolean includeInactivePolicies;

  public OpenConextEvaluationContextFactory() throws IOException {
    this.cachePolicies = Boolean.valueOf(XACMLProperties.getProperties().getProperty("openconext.pdp.cachePolicies", "true"));
    this.includeInactivePolicies = Boolean.valueOf(XACMLProperties.getProperties().getProperty("openconext.pdp.includeInactivePolicies", "false"));
  }

  public void injectPolicyFinderDependencies(PdpPolicyRepository pdpPolicyRepository) {
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
    Collection<PolicySetChild> policies =
        stream(pdpPolicyRepository.findAll().spliterator(), false)
            .filter(policy -> policy.isActive() || includeInactivePolicies)
            .map(policy -> (PolicySetChild) convertToPolicyDef(policy))
            .collect(toCollection(ArrayList::new));
    LOG.info("(Re)-loaded {} policies from the database", policies.size());
    try {
      return new StdPolicyFinder(combinePolicies(policies), null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private PolicySet combinePolicies(Collection<PolicySetChild> policies) throws IOException, FactoryException, ParseException {
    String combiningAlgorithm = XACMLProperties.getProperties().getProperty(
        OpenAZPDPProperties.PROP_POLICYFINDERFACTORY_COMBINEROOTPOLICIES, "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides");
    CombiningAlgorithm<PolicySetChild> algorithm = CombiningAlgorithmFactory.newInstance()
        .getPolicyCombiningAlgorithm(new IdentifierImpl(combiningAlgorithm));

    LOG.info("Combining root policies with " + algorithm);

    PolicySet root = new PolicySet();
    root.setIdentifier(new IdentifierImpl("urn:openconext:pdp:root:policyset"));
    root.setVersion(StdVersion.newInstance("1.0"));
    root.setTarget(new Target());
    root.setPolicyCombiningAlgorithm(algorithm);
    root.setChildren(policies);
    return root;
  }

  private PolicyDef convertToPolicyDef(PdpPolicy pdpPolicy) {
    Policy policyDef = PdpPolicyDefinitionParser.parsePolicy(pdpPolicy.getPolicyXml());
    policyDef.setIdentifier(new IdentifierImpl(getPolicyId(pdpPolicy.getName())));
    policyDef.getRules().forEachRemaining(rule -> {
          if (rule.getRuleEffect().getDecision().equals(Decision.DENY)) {
            rule.getAdviceExpressions().forEachRemaining(adviceExpression ->
                    adviceExpression.setAdviceId(new IdentifierImpl(getPolicyId(pdpPolicy.getName())))
            );
          }
        }
    );
    return policyDef;
  }

  public void injectPIPFinderDependencies(VootClient vootClient, SabClient sabClient) {
    setPIPFinder(loadPIPFinder(vootClient, sabClient));
  }

  private PIPFinder loadPIPFinder(VootClient vootClient, SabClient sabClient) {
    OpenConextConfigurableEngineFinder pipFinder = new OpenConextConfigurableEngineFinder(vootClient, sabClient);
    try {
      pipFinder.configure(XACMLProperties.getProperties());
      return pipFinder;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
