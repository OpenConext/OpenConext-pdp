package pdp.serviceregistry;

import org.junit.Test;
import org.springframework.util.StringUtils;
import pdp.access.PolicyIdpAccessUnknownIdentityProvidersException;
import pdp.domain.EntityMetaData;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassPathResourceServiceRegistryTest {

    private static ServiceRegistry serviceRegistry = new ClassPathResourceServiceRegistry(true);

    @Test
    public void testServiceProviders() throws Exception {
        List<EntityMetaData> sps = serviceRegistry.serviceProviders();
        assertEquals(49, sps.size());
        assertTrue(sps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
        // we expect a sorted list
        assertEquals(asList("B", "B", "D"), sps.subList(0, 3).stream().map(e -> e.getNameEn().substring(0, 1)).collect(toList()));
    }

    @Test
    public void testIdentityProviders() throws Exception {
        List<EntityMetaData> idps = serviceRegistry.identityProviders();
        assertEquals(13, idps.size());
        assertTrue(idps.stream().allMatch(entityMetaData -> StringUtils.hasText(entityMetaData.getEntityId())));
    }

    @Test
    public void testInstitutionId() throws Exception {
        EntityMetaData sara = serviceRegistry.identityProviderByEntityId("http://mock-idp");
        assertEquals("surfconext", sara.getInstitutionId());
    }

    @Test
    public void testIdentityProvidersByAuthenticatingAuthority() throws Exception {
        Set<EntityMetaData> idps = serviceRegistry.identityProvidersByAuthenticatingAuthority("https://idp.surfnet.nl");
        assertEquals(1, idps.size());
    }

    @Test(expected = PolicyIdpAccessUnknownIdentityProvidersException.class)
    public void testIdentityProvidersByAuthenticatingAuthorityNonExistentIdp() throws Exception {
        serviceRegistry.identityProvidersByAuthenticatingAuthority("no");
    }

    @Test
    public void testServiceProvidersByInstitutionId() {
        Set<EntityMetaData> surfnetSps = serviceRegistry.serviceProvidersByInstitutionId("surfconext");
        assertEquals(1, surfnetSps.size());
        assertEquals(1, surfnetSps.stream().filter(sp -> sp.isPolicyEnforcementDecisionRequired()).count());
    }

    @Test
    public void testServiceProvidersByInstitutionIdNull() {
        Set<EntityMetaData> sps = serviceRegistry.serviceProvidersByInstitutionId(null);
        assertEquals(0, sps.size());
    }

    @Test
    public void testServiceProvidersByInstitutionIdEmpty() {
        Set<EntityMetaData> sps = serviceRegistry.serviceProvidersByInstitutionId("NOOP");
        assertEquals(0, sps.size());
    }


}