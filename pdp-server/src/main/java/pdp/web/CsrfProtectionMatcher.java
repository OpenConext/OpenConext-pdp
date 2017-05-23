package pdp.web;

import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static pdp.access.FederatedUserBuilder.X_DISPLAY_NAME;
import static pdp.access.FederatedUserBuilder.X_IDP_ENTITY_ID;
import static pdp.access.FederatedUserBuilder.X_UNSPECIFIED_NAME_ID;

public class CsrfProtectionMatcher implements RequestMatcher {

    private final Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
    private final List<String> trustedApiHttpHeaders = asList(X_IDP_ENTITY_ID, X_UNSPECIFIED_NAME_ID, X_DISPLAY_NAME);

    @Override
    public boolean matches(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return servletPath.startsWith("/internal")
            && !servletPath.equalsIgnoreCase("/internal/jsError")
            && !isTrustedApiRequest(request)
            && !allowedMethods.matcher(request.getMethod().toUpperCase()).matches();
    }

    private boolean isTrustedApiRequest(HttpServletRequest request) {
        //we can't expect server to server trusted calls to send CSRF parameters
        return trustedApiHttpHeaders.stream().map(s -> request.getHeader(s)).allMatch(v -> v != null);
    }
}
