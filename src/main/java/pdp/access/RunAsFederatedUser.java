package pdp.access;

import org.springframework.security.core.GrantedAuthority;
import pdp.domain.EntityMetaData;

import java.util.Collection;
import java.util.Set;

@SuppressWarnings("serial")
public class RunAsFederatedUser extends FederatedUser {

    public RunAsFederatedUser(String uid, String authenticatingAuthority, String displayName, Set<EntityMetaData> idpEntities, Set<EntityMetaData> spEntities, Collection<? extends GrantedAuthority> authorities) {
        super(uid, authenticatingAuthority, displayName, idpEntities, spEntities, authorities);
    }

    @Override
    public boolean isPolicyIdpAccessEnforcementRequired() {
        return true;
    }
}
