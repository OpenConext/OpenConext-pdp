package pdp.web;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import pdp.access.PolicyIdpAccessAwareToken;
import pdp.access.RunAsFederatedUser;
import pdp.domain.EntityMetaData;
import pdp.manage.ClassPathResourceManage;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ManageControllerTest {

    private ManageController subject = new ManageController(new ClassPathResourceManage());

    @Test
    public void testServiceProviders() throws Exception {
        assertEquals(51, subject.serviceProviders().size());
    }

    @Test
    public void testIdentityProviders() throws Exception {
        assertEquals(13, subject.identityProviders().size());
    }

    @Test
    public void testIdentityProvidersScoped() throws Exception {
        setupSecurityContext(Collections.emptySet(), Collections.emptySet());
        assertEquals(0, subject.identityProvidersScoped().size());

    }

    private void setupSecurityContext(Set<EntityMetaData> idpEntities, Set<EntityMetaData> spEntities) {
        SecurityContext context = new SecurityContextImpl();
        Authentication authentication = new PolicyIdpAccessAwareToken(
            new RunAsFederatedUser("uid", "unknown-idp", "John Doe", idpEntities, spEntities, Collections.emptyList()));
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}