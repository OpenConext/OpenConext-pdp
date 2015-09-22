package pdp.xacml;

import org.apache.openaz.xacml.api.pip.PIPEngine;
import org.apache.openaz.xacml.std.pip.finders.ConfigurableEngineFinder;
import pdp.xacml.teams.VootClient;
import pdp.xacml.teams.VootClientAware;

public class OpenConextConfigurableEngineFinder extends ConfigurableEngineFinder {

  private final VootClient vootClient;

  public OpenConextConfigurableEngineFinder(VootClient vootClient) {
    this.vootClient = vootClient;
  }

  @Override
  public void register(PIPEngine pipEngine) {
    if (pipEngine instanceof VootClientAware) {
      ((VootClientAware) pipEngine).setVootClient(this.vootClient);
    }
    super.register(pipEngine);
  }
}
