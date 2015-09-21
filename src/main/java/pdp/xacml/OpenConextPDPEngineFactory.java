package pdp.xacml;

import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.OpenAZPDPEngine;
import org.apache.openaz.xacml.pdp.OpenAZPDPEngineFactory;
import org.apache.openaz.xacml.pdp.eval.EvaluationContextFactory;
import org.apache.openaz.xacml.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.PdpPolicyRepository;

import java.io.IOException;
import java.util.Properties;

public class OpenConextPDPEngineFactory extends OpenAZPDPEngineFactory {

  public PDPEngine newEngine(PdpPolicyRepository pdpPolicyRepository) throws FactoryException, IOException {
    EvaluationContextFactory evaluationContextFactory = EvaluationContextFactory.newInstance();
    /*
     * Need to do this to remain property driven as OpenAZ is designed and be able to inject dependencies
     */
    if (evaluationContextFactory instanceof RepositoryEvaluationContextFactory) {
      RepositoryEvaluationContextFactory factory = (RepositoryEvaluationContextFactory) evaluationContextFactory;
      factory.setPdpPolicyRepository(pdpPolicyRepository);
    }
    return new OpenAZPDPEngine(evaluationContextFactory, this.getDefaultBehavior(), this.getScopeResolver());
  }


}
