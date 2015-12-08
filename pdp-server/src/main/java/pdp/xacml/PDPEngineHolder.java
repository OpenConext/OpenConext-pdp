package pdp.xacml;

import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.repositories.PdpPolicyRepository;
import pdp.teams.VootClient;

public class PDPEngineHolder {

  private static Logger LOG = LoggerFactory.getLogger(PDPEngineHolder.class);

  private PdpPolicyRepository pdpPolicyRepository;
  private VootClient vootClient;

  public PDPEngineHolder(PdpPolicyRepository pdpPolicyRepository, VootClient vootClient) {
    this.pdpPolicyRepository = pdpPolicyRepository;
    this.vootClient = vootClient;
  }

  public PDPEngine newPdpEngine(boolean policyIncludeAggregatedAttributes) {
    try {
      PDPEngineFactory factory = PDPEngineFactory.newInstance();

      //We want to be properties driven for testability, but we can't otherwise hook into the PdpPolicyRepository
      if (factory instanceof OpenConextPDPEngineFactory) {
        return ((OpenConextPDPEngineFactory) factory).newEngine(policyIncludeAggregatedAttributes, pdpPolicyRepository, vootClient);
      } else {
        return factory.newEngine();
      }
    } catch (Exception e) {
      LOG.error("Exception while re-creating PDPEngine", e);
      throw new RuntimeException(e);
    }
  }

}
