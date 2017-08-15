package pdp.teams;

import pdp.xacml.AbstractConfigurableEngine;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TeamsPIP extends AbstractConfigurableEngine implements VootClientAware {

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
    protected List<Object> getAttributes(String userUrn) {
        return getVootClient().groups(userUrn).stream().collect(Collectors.toList());
    }

    public VootClient getVootClient() {
        return vootClient;
    }

    public void setVootClient(VootClient vootClient) {
        this.vootClient = vootClient;
    }

}
