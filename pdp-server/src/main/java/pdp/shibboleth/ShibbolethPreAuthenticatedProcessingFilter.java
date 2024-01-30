package pdp.shibboleth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;
import pdp.access.FederatedUser;
import pdp.access.FederatedUserBuilder;
import pdp.access.RunAsFederatedUser;
import pdp.manage.Manage;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static pdp.access.FederatedUserBuilder.X_IMPERSONATE;
import static pdp.access.FederatedUserBuilder.apiAuthorities;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final FederatedUserBuilder federatedUserBuilder;

    public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager, Manage manage) {
        super();
        setAuthenticationManager(authenticationManager);
        this.federatedUserBuilder = new FederatedUserBuilder(manage);
        setCheckForPrincipalChanges(true);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        Optional<FederatedUser> federatedUser = federatedUserBuilder.shibUser(request);
        if (!federatedUser.isPresent()) {
            //null is how the contract for AbstractPreAuthenticatedProcessingFilter works
            return null;
        }
        //Now we are certain a shib admin user is logged in, and we can check if there is impersonation requested
        if (hasText(getHeader(X_IMPERSONATE, request))) {
            federatedUser = federatedUserBuilder.basicAuthUser(request, apiAuthorities);
        }
        return federatedUser.isPresent() ? federatedUser.get() : null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    @Override
    protected boolean principalChanged(HttpServletRequest request, Authentication currentAuthentication) {
        //the Javascript client has the functionality to impersonate a user. If this functionality is off then
        //only need to check if the currentAuthentication is not a previous cached impersonation
        return hasText(getHeader(X_IMPERSONATE, request)) || currentAuthentication.getPrincipal() instanceof RunAsFederatedUser;
    }

    private String getHeader(String name, HttpServletRequest request) {
        String header = request.getHeader(name);
        try {
            return StringUtils.hasText(header) ?
                    new String(header.getBytes("ISO8859-1"), "UTF-8") : header;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
