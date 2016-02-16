package pdp.xacml;

import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.OpenAZPDPEngineFactory;
import org.apache.openaz.xacml.pdp.eval.EvaluationContextFactory;
import org.apache.openaz.xacml.util.FactoryException;
import pdp.repositories.PdpPolicyRepository;
import pdp.sab.SabClient;
import pdp.teams.VootClient;

import java.io.IOException;

public class OpenConextPDPEngineFactory extends OpenAZPDPEngineFactory {

  public PDPEngine newEngine(boolean cachePolicies,
                             boolean includeInactivePolicies,
                             PdpPolicyRepository pdpPolicyRepository,
                             VootClient vootClient,
                             SabClient sabClient) throws FactoryException, IOException {
    EvaluationContextFactory evaluationContextFactory = EvaluationContextFactory.newInstance();
    /**
     * Need to do this to remain property driven as OpenAZ is designed and be able to inject dependencies
     */
    if (evaluationContextFactory instanceof OpenConextEvaluationContextFactory) {
      OpenConextEvaluationContextFactory factory = (OpenConextEvaluationContextFactory) evaluationContextFactory;
      factory.injectPolicyFinderDependencies(pdpPolicyRepository);
      factory.injectPIPFinderDependencies(vootClient, sabClient);
      factory.setCachePolicies(cachePolicies);
      factory.setIncludeInactivePolicies(includeInactivePolicies);
    }
    return new OpenConextPDPEngine(evaluationContextFactory, this.getDefaultBehavior(), this.getScopeResolver());
  }


}
