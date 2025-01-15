package pdp.access;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import pdp.manage.Manage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class PolicyIdpAccessEnforcerFilter extends BasicAuthenticationFilter {

    private final FederatedUserBuilder federatedUserBuilder;

    public PolicyIdpAccessEnforcerFilter(AuthenticationManager authenticationManager, Manage manage) {
        super(authenticationManager);
        this.federatedUserBuilder = new FederatedUserBuilder(manage);
    }

    @Override
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
        Optional<FederatedUser> runAsFederatedUserOptional = federatedUserBuilder.basicAuthUser(request, authResult.getAuthorities());
        runAsFederatedUserOptional
                .ifPresent(federatedUser -> {
                    PolicyIdpAccessAwareToken authentication = new PolicyIdpAccessAwareToken((RunAsFederatedUser) federatedUser);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
    }

}
