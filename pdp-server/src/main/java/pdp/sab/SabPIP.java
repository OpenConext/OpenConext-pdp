package pdp.sab;

import pdp.xacml.AbstractConfigurableEngine;

import java.io.IOException;
import java.util.List;

public class SabPIP extends AbstractConfigurableEngine implements SabClientAware {

    private SabClient sabClient;

    public static final String SAB_URN = "urn:collab:sab:surfnet.nl";

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
    protected List<String> getAttributes(String userUrn) {
        try {
            return getSabClient().roles(userUrn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SabClient getSabClient() {
        return sabClient;
    }

    @Override
    public void setSabClient(SabClient sabClient) {
        this.sabClient = sabClient;
    }
}
