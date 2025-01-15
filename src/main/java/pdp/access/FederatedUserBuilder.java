package pdp.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import pdp.domain.EntityMetaData;
import pdp.manage.Manage;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.util.StringUtils.isEmpty;

@SuppressWarnings("deprecation")
public class FederatedUserBuilder {

    private static final Collection<? extends GrantedAuthority> shibAuthorities = createAuthorityList("ROLE_USER", "ROLE_ADMIN");

    public static final Collection<? extends GrantedAuthority> apiAuthorities = createAuthorityList("ROLE_USER", "ROLE_PEP");

    //shib headers
    public static final String UID_HEADER_NAME = "uid";
    public static final String DISPLAY_NAME_HEADER_NAME = "displayname";
    public static final String SHIB_AUTHENTICATING_AUTHORITY = "Shib-Authenticating-Authority";

    //trusted API headers
    public static final String X_IDP_ENTITY_ID = "X-IDP-ENTITY-ID";
    public static final String X_UNSPECIFIED_NAME_ID = "X-UNSPECIFIED-NAME-ID";
    public static final String X_DISPLAY_NAME = "X-DISPLAY-NAME";

    //impersonate header
    public static final String X_IMPERSONATE = "X-IMPERSONATE";

    private static final Logger LOG = LoggerFactory.getLogger(FederatedUserBuilder.class);

    private final Manage manage;

    public FederatedUserBuilder(Manage manage) {
        this.manage = manage;
    }

    public Optional<FederatedUser> basicAuthUser(HttpServletRequest request, Collection<? extends GrantedAuthority> authorities) {
        //check headers for enrichment of the Authentication
        String idpEntityId = getHeader(X_IDP_ENTITY_ID, request, false);
        String nameId = getHeader(X_UNSPECIFIED_NAME_ID, request, false);
        String displayName = getHeader(X_DISPLAY_NAME, request, false);

        if (isEmpty(idpEntityId) || isEmpty(nameId) || isEmpty(displayName)) {
            //any policy idp access checks will fail, but it might be that this call is not for something that requires access
            return Optional.empty();
        }

        Set<EntityMetaData> idpEntities = manage.identityProvidersByAuthenticatingAuthority(idpEntityId);
        Set<EntityMetaData> spEntities = getSpEntities(idpEntities);

        LOG.debug("Creating RunAsFederatedUser {}", nameId);

        return Optional.of(new RunAsFederatedUser(nameId, idpEntityId, displayName, idpEntities, spEntities, authorities));
    }

    public Optional<FederatedUser> shibUser(HttpServletRequest request) {
        String uid = getHeader(UID_HEADER_NAME, request, true);
        String displayName = getHeader(DISPLAY_NAME_HEADER_NAME, request, true);
        String authenticatingAuthority = getHeader(SHIB_AUTHENTICATING_AUTHORITY, request, true);

        if (isEmpty(uid) || isEmpty(displayName) || isEmpty(authenticatingAuthority)) {
            return Optional.empty();
        }

        //By contract we always get at least one Idp and usually two separated by a semi-colon
        authenticatingAuthority = authenticatingAuthority.split(";")[0];
        Set<EntityMetaData> idpEntities;
        try {
            idpEntities = manage.identityProvidersByAuthenticatingAuthority(authenticatingAuthority);
        } catch (PolicyIdpAccessUnknownIdentityProvidersException e) {
            return Optional.empty();
        }

        Set<EntityMetaData> spEntities = getSpEntities(idpEntities);

        LOG.debug("Creating FederatedUser {}", uid);

        return Optional.of(new FederatedUser(uid, authenticatingAuthority, displayName, idpEntities, spEntities, shibAuthorities));
    }

    private Set<EntityMetaData> getSpEntities(Set<EntityMetaData> idpEntities) {
        //By contract, we have at least one Idp - otherwise an Exception is already raised
        String institutionId = idpEntities.iterator().next().getInstitutionId();
        return manage.serviceProvidersByInstitutionId(institutionId);
    }

    private String getHeader(String name, HttpServletRequest request, boolean convertFromIso88591) {
        String header = request.getHeader(name);
        if (!convertFromIso88591) {
            return header;
        }
        try {
            return StringUtils.hasText(header) ?
                    new String(header.getBytes("ISO8859-1"), "UTF-8") : header;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
