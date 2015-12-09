package pdp.xacml;

import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.repositories.PdpPolicyRepository;
import pdp.sab.SabClient;
import pdp.teams.VootClient;

public class PDPEngineHolder {

  private static Logger LOG = LoggerFactory.getLogger(PDPEngineHolder.class);
  private final SabClient sabClient;

  private PdpPolicyRepository pdpPolicyRepository;
  private VootClient vootClient;

  public PDPEngineHolder(PdpPolicyRepository pdpPolicyRepository, VootClient vootClient, SabClient sabClient) {
    this.pdpPolicyRepository = pdpPolicyRepository;
    this.vootClient = vootClient;
    this.sabClient = sabClient;
  }

  public PDPEngine newPdpEngine(boolean policyIncludeAggregatedAttributes) {
    try {
      PDPEngineFactory factory = PDPEngineFactory.newInstance();

      //We stick to the properties driven design of open-az, but we can't otherwise hook into the needed dependencies
      if (factory instanceof OpenConextPDPEngineFactory) {
        return ((OpenConextPDPEngineFactory) factory).newEngine(policyIncludeAggregatedAttributes, pdpPolicyRepository, vootClient, sabClient);
      } else {
        return factory.newEngine();
      }
    } catch (Exception e) {
      LOG.error("Exception while re-creating PDPEngine", e);
      throw new RuntimeException(e);
    }
  }

}
