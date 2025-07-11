package pdp.access;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.util.Assert.notNull;

/**
 * EngineBlock and Dashboard call the PDP and we don't want to use OAuth for this as
 * they are trusted clients
 */
public class BasicAuthenticationProvider implements AuthenticationManager {

    private static final Collection<? extends GrantedAuthority> API_AUTHORITIES = createAuthorityList("ROLE_USER", "ROLE_PEP");


    private final String userName;
    private final String password;

    public BasicAuthenticationProvider(String userName, String password) {
        notNull(userName, "Username must not be null");
        notNull(password, "Password must not be null");

        this.userName = userName;
        this.password = password;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //the exceptions are for logging and are not propagated to the end user / application
        if (!userName.equals(authentication.getPrincipal())) {
            throw new UsernameNotFoundException("Unknown user: " + authentication.getPrincipal());
        }
        if (!password.equals(authentication.getCredentials())) {
            throw new BadCredentialsException("Bad credentials");
        }
        return new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                API_AUTHORITIES);
    }

}
