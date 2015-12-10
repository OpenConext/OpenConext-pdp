package pdp.teams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.xacml.AbstractConfigurableEngine;

import java.util.List;

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
