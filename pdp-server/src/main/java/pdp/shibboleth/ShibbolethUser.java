package pdp.shibboleth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import pdp.domain.EntityMetaData;

import java.util.Collection;
import java.util.Set;

public class ShibbolethUser extends User {

  private final String uid;
  private final Set<EntityMetaData> idpEntities;
  private final Set<EntityMetaData> spEntities;

  public ShibbolethUser(String uid, String username, Set<EntityMetaData> idpEntities, Set<EntityMetaData> spEntities, Collection<? extends GrantedAuthority> authorities) {
    super(username, "N/A", authorities);
    this.uid = uid;
    this.idpEntities = idpEntities;
    this.spEntities = spEntities;
  }

  public String getUid() {
    return uid;
  }

  public Set<EntityMetaData> getIdpEntities() {
    return idpEntities;
  }

  public Set<EntityMetaData> getSpEntities() {
    return spEntities;
  }
}
