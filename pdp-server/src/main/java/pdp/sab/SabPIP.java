package pdp.sab;

import pdp.xacml.AbstractConfigurableEngine;

import java.util.List;
import java.util.stream.Collectors;

public class SabPIP extends AbstractConfigurableEngine implements SabClientAware {

    public static final String SAB_URN = "urn:collab:sab:surfnet.nl";
    private SabClient sabClient;

    @Override
    public String getName() {
        return "sab_pip";
    }

    @Override
    public String getDescription() {
        return "Sab Policy Information Point";
    }

    @Override
    public String getIdentifierProvidedAttribute() {
        return SAB_URN;
    }

    @Override
    protected List<Object> getAttributes(String userUrn) {
        return getSabClient().roles(userUrn).stream().collect(Collectors.toList());
    }

    public SabClient getSabClient() {
        return sabClient;
    }

    @Override
    public void setSabClient(SabClient sabClient) {
        this.sabClient = sabClient;
    }
}
