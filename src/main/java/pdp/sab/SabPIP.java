package pdp.sab;

import lombok.Getter;
import lombok.Setter;
import pdp.xacml.AbstractConfigurableEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SabPIP extends AbstractConfigurableEngine implements SabClientAware {

    public static final String SAB_URN = "urn:collab:sab:surfnet.nl";

    @Getter
    @Setter
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
        return new ArrayList<>(getSabClient().roles(userUrn));
    }

    @Override
    public boolean useUnspecifiedURN() {
        return false;
    }
}
