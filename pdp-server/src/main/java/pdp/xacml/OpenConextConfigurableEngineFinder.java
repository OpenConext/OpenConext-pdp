package pdp.xacml;

import org.apache.openaz.xacml.api.pip.PIPEngine;
import org.apache.openaz.xacml.std.pip.finders.ConfigurableEngineFinder;
import pdp.sab.SabClient;
import pdp.sab.SabClientAware;
import pdp.teams.VootClient;
import pdp.teams.VootClientAware;

public class OpenConextConfigurableEngineFinder extends ConfigurableEngineFinder {

    private final VootClient vootClient;
    private final SabClient sabClient;

    public OpenConextConfigurableEngineFinder(VootClient vootClient, SabClient sabClient) {
        this.vootClient = vootClient;
        this.sabClient = sabClient;
    }

    @Override
    public void register(PIPEngine pipEngine) {
        if (pipEngine instanceof VootClientAware) {
            ((VootClientAware) pipEngine).setVootClient(this.vootClient);
        }
        if (pipEngine instanceof SabClientAware) {
            ((SabClientAware) pipEngine).setSabClient(this.sabClient);
        }
        super.register(pipEngine);
    }

}
