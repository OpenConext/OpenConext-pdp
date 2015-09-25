package pdp.shibboleth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {


  public static class ShibbolethPrincipal {
    public final String uid;
    public final String displayName;

    public ShibbolethPrincipal(String uid, String displayName) {
      this.uid = uid;
      this.displayName = displayName;
    }

    @Override
    public String toString() {
      return "ShibbolethPrincipal{" +
        "uid='" + uid + '\'' +
        ", displayName='" + displayName + '\'' +
        '}';
    }
  }

  public static final String UID_HEADER_NAME = "uid";
  public static final String DISPLAY_NAME_HEADER_NAME = "displayname";

  public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager) {
    super();
    setAuthenticationManager(authenticationManager);
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
    String uid = request.getHeader(UID_HEADER_NAME);
    String displayName = request.getHeader(DISPLAY_NAME_HEADER_NAME);
    return StringUtils.hasText(uid) && StringUtils.hasText(displayName) ?
        new ShibbolethPrincipal(uid, displayName) : null;
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }
}
