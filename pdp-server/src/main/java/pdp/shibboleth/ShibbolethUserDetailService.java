package pdp.shibboleth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collection;
import java.util.Collections;

import static pdp.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.*;

public class ShibbolethUserDetailService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

  public static class ShibbolethUser implements UserDetails {

    private final String uid;
    private final String displayName;

    public ShibbolethUser(String uid, String displayName) {
      this.uid = uid;
      this.displayName = displayName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return Collections.EMPTY_LIST;
    }

    @Override
    public String getPassword() {
      return null;
    }

    @Override
    public String getUsername() {
      return this.uid;
    }

    public String getUid() {
      return uid;
    }

    public String getDisplayName() {
      return displayName;
    }

    @Override
    public boolean isAccountNonExpired() {
      return true;
    }

    @Override
    public boolean isAccountNonLocked() {
      return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }
  }

  @Override
  public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authentication) throws UsernameNotFoundException {
    ShibbolethPrincipal principal = (ShibbolethPrincipal) authentication.getPrincipal();
    return new ShibbolethUser(principal.uid, principal.displayName);
  }
}
