package pdp.xacml;

import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.repositories.PdpPolicyRepository;
import pdp.sab.SabClient;
import pdp.teams.VootClient;

import java.io.IOException;

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

  public PDPEngine newPdpEngine(boolean cachePolicies, boolean includeInactivePolicies) {
    try {
      OpenConextPDPEngineFactory factory = new OpenConextPDPEngineFactory();
      return factory.newEngine(cachePolicies, includeInactivePolicies, pdpPolicyRepository, vootClient, sabClient);
    } catch (IOException | FactoryException e) {
      LOG.error("Exception while re-creating PDPEngine", e);
      throw new RuntimeException(e);
    }
  }

}
