package pdp.teams;

import org.apache.openaz.xacml.api.Attribute;
import org.apache.openaz.xacml.api.AttributeValue;
import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.api.pip.PIPException;
import org.apache.openaz.xacml.api.pip.PIPFinder;
import org.apache.openaz.xacml.api.pip.PIPRequest;
import org.apache.openaz.xacml.api.pip.PIPResponse;
import org.apache.openaz.xacml.std.*;
import org.apache.openaz.xacml.std.pip.StdMutablePIPResponse;
import org.apache.openaz.xacml.std.pip.StdPIPRequest;
import org.apache.openaz.xacml.std.pip.StdSinglePIPResponse;
import org.apache.openaz.xacml.std.pip.engines.ConfigurableEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import pdp.xacml.AbstractConfigurableEngine;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static pdp.xacml.PdpPolicyDefinitionParser.NAME_ID;

public class TeamsPIP extends AbstractConfigurableEngine implements VootClientAware {

  private final static Logger LOG = LoggerFactory.getLogger(TeamsPIP.class);

  public static final String GROUP_URN = "urn:collab:group:surfteams.nl";

  private VootClient vootClient;

  @Override
  public String getName() {
    return "teams_pip";
  }

  @Override
  public String getDescription() {
    return "Teams Policy Information Point";
  }

  @Override
  public String getIdentifierProvidedAttribute() {
    return GROUP_URN;
  }

  @Override
  protected List<String> getAttributes(String userUrn) {
    return getVootClient().groups(userUrn);
  }

  public VootClient getVootClient() {
    return vootClient;
  }

  public void setVootClient(VootClient vootClient) {
    this.vootClient = vootClient;
  }

}
