package pdp.shibboleth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import pdp.domain.EntityMetaData;

import java.util.Collection;
import java.util.Set;

public class ShibbolethUser extends User {

  private final String displayName;
  private final Set<EntityMetaData> idpEntities;
  private final Set<EntityMetaData> spEntities;
  private final String authenticatingAuthority;
  private final boolean policyIdpAccessEnforcement;

  public ShibbolethUser(String uid, String authenticatingAuthority, String displayName, Set<EntityMetaData> idpEntities, Set<EntityMetaData> spEntities, Collection<? extends GrantedAuthority> authorities, boolean policyIdpAccessEnforcement) {
    super(uid, "N/A", authorities);
    this.displayName = displayName;
    this.authenticatingAuthority = authenticatingAuthority;
    this.idpEntities = idpEntities;
    this.spEntities = spEntities;
    this.policyIdpAccessEnforcement = policyIdpAccessEnforcement;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getAuthenticatingAuthority() {
    return authenticatingAuthority;
  }

  public Set<EntityMetaData> getIdpEntities() {
    return idpEntities;
  }

  public Set<EntityMetaData> getSpEntities() {
    return spEntities;
  }

  public boolean isPolicyIdpAccessEnforcement() {
    return policyIdpAccessEnforcement;
  }
}
