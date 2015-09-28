package pdp.shibboleth;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ShibbolethUser extends org.springframework.security.core.userdetails.User {

  private final String uid;

  public ShibbolethUser(String uid, String username, Collection<? extends GrantedAuthority> authorities) {
    super(username, "N/A", authorities);
    this.uid = uid;
  }

  public String getUid() {
    return uid;
  }
}
