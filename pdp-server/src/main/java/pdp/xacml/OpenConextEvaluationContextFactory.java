package pdp.xacml;

import org.apache.openaz.xacml.api.pip.PIPFinder;
import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.pdp.std.StdEvaluationContextFactory;
import org.apache.openaz.xacml.pdp.std.StdPolicyFinder;
import org.apache.openaz.xacml.pdp.util.OpenAZPDPProperties;
import org.apache.openaz.xacml.std.IdentifierImpl;
import org.apache.openaz.xacml.std.StdStatusCode;
import org.apache.openaz.xacml.std.StdVersion;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.apache.openaz.xacml.util.FactoryException;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.repositories.PdpPolicyRepository;
import pdp.teams.VootClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.StreamSupport.stream;

public class OpenConextEvaluationContextFactory extends StdEvaluationContextFactory {

  private static Logger LOG = LoggerFactory.getLogger(OpenConextEvaluationContextFactory.class);

  private PdpPolicyRepository pdpPolicyRepository;
  private boolean cachePolicies;

  public OpenConextEvaluationContextFactory() throws IOException {
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

  private PolicyFinder loadPolicyFinder()  {
    Collection<PolicySetChild> polices =
        stream(pdpPolicyRepository.findAll().spliterator(), false).map(policy -> (PolicySetChild) convertToPolicyDef(policy.getPolicyXml())).collect(toCollection(ArrayList::new));
    LOG.info("(Re)-loaded {} policies from the database", polices.size());
    try {
      return new StdPolicyFinder(combinePolicies(polices), null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private PolicySet combinePolicies(Collection<PolicySetChild> policies) throws IOException, FactoryException, ParseException {
    String combiningAlgorithm = XACMLProperties.getProperties().getProperty(OpenAZPDPProperties.PROP_POLICYFINDERFACTORY_COMBINEROOTPOLICIES, "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides");
    CombiningAlgorithm<PolicySetChild> algorithm = CombiningAlgorithmFactory.newInstance()
        .getPolicyCombiningAlgorithm(new IdentifierImpl(combiningAlgorithm));

    LOG.info("Combining root policies with " + algorithm);

    PolicySet root = new PolicySet();
    root.setIdentifier(new IdentifierImpl("urn:openconext:pdp:root::policyset"));
    root.setVersion(StdVersion.newInstance("1.0"));
    root.setTarget(new Target());
    root.setPolicyCombiningAlgorithm(algorithm);
    root.setChildren(policies);
    return root;
  }

  private PolicyDef convertToPolicyDef(String policyXml) {
    try {
      return DOMPolicyDef.load(new ByteArrayInputStream(policyXml.replaceFirst("\n", "").getBytes()));
    } catch (DOMStructureException e) {
      LOG.error("Error loading policy from " + policyXml, e);
      return new Policy(StdStatusCode.STATUS_CODE_SYNTAX_ERROR, e.getMessage());
    }
  }

  public void setVootClient(VootClient vootClient) {
    setPIPFinder(loadPIPFinder(vootClient));
  }

  private PIPFinder loadPIPFinder(VootClient vootClient) {
    OpenConextConfigurableEngineFinder pipFinder = new OpenConextConfigurableEngineFinder(vootClient);
    try {
      pipFinder.configure(XACMLProperties.getProperties());
      return pipFinder;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
