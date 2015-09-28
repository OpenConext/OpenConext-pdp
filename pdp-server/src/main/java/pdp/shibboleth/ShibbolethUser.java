package pdp.shibboleth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class ShibbolethUser extends User {

  private final String uid;
  private final String schacHomeOrganization;

  public ShibbolethUser(String uid, String username, String schacHomeOrganization, Collection<? extends GrantedAuthority> authorities) {
    super(username, "N/A", authorities);
    this.uid = uid;
    this.schacHomeOrganization = schacHomeOrganization;
  }

  public String getUid() {
    return uid;
  }

  public String getSchacHomeOrganization() {
    return schacHomeOrganization;
  }
}
