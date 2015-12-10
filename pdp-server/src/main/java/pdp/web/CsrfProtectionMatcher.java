package pdp.web;

import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static pdp.access.PolicyIdpAccessEnforcerFilter.*;
public class CsrfProtectionMatcher implements RequestMatcher {

  private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
  private List<String> trustedApiHttpHeaders = asList(X_IDP_ENTITY_ID, X_UNSPECIFIED_NAME_ID, X_DISPLAY_NAME);

  @Override
  public boolean matches(HttpServletRequest request) {
    return request.getServletPath().startsWith("/internal")
        && !isTrustedApiRequest(request)
        && !allowedMethods.matcher(request.getMethod().toUpperCase()).matches();
  }

  private boolean isTrustedApiRequest(HttpServletRequest request) {
    //we can't expect server to server trusted calls to send CSRF parameters
    return trustedApiHttpHeaders.stream().map(s -> request.getHeader(s)).allMatch(v -> v != null);

  }
}
