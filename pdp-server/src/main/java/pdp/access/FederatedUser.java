package pdp.access;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import pdp.domain.EntityMetaData;

import java.util.Collection;
import java.util.Set;

public class FederatedUser extends User {

  private final String displayName;
  private final Set<EntityMetaData> idpEntities;
  private final Set<EntityMetaData> spEntities;
  private final String authenticatingAuthority;

  public FederatedUser(String uid, String authenticatingAuthority, String displayName, Set<EntityMetaData> idpEntities, Set<EntityMetaData> spEntities, Collection<? extends GrantedAuthority> authorities) {
    super(uid, "N/A", authorities);
    this.displayName = displayName;
    this.authenticatingAuthority = authenticatingAuthority;
    this.idpEntities = idpEntities;
    this.spEntities = spEntities;
  }

  public Set<EntityMetaData> getIdpEntities() {
    return idpEntities;
  }

  public Set<EntityMetaData> getSpEntities() {
    return spEntities;
  }

  public boolean isPolicyIdpAccessEnforcementRequired() {
    return false;
  }

  public String getIdentifier() {
    return getUsername();
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getAuthenticatingAuthority() {
    return authenticatingAuthority;
  }

}
