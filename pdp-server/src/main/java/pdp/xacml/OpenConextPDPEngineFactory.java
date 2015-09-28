package pdp.xacml;

import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.pdp.OpenAZPDPEngine;
import org.apache.openaz.xacml.pdp.OpenAZPDPEngineFactory;
import org.apache.openaz.xacml.pdp.eval.EvaluationContextFactory;
import org.apache.openaz.xacml.util.FactoryException;
import pdp.repositories.PdpPolicyRepository;
import pdp.teams.VootClient;

import java.io.IOException;

public class OpenConextPDPEngineFactory extends OpenAZPDPEngineFactory {

    public PDPEngine newEngine(PdpPolicyRepository pdpPolicyRepository, VootClient vootClient) throws FactoryException, IOException {
        EvaluationContextFactory evaluationContextFactory = EvaluationContextFactory.newInstance();
    /*
     * Need to do this to remain property driven as OpenAZ is designed and be able to inject dependencies
     */
        if (evaluationContextFactory instanceof OpenConextEvaluationContextFactory) {
            OpenConextEvaluationContextFactory factory = (OpenConextEvaluationContextFactory) evaluationContextFactory;
            factory.setPdpPolicyRepository(pdpPolicyRepository);
            factory.setVootClient(vootClient);
        }
        return new OpenAZPDPEngine(evaluationContextFactory, this.getDefaultBehavior(), this.getScopeResolver());
    }


}
