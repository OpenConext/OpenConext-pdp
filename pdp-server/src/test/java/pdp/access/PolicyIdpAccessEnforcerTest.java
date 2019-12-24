package pdp.access;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import pdp.JsonMapper;
import pdp.domain.EntityMetaData;
import pdp.domain.JsonPolicyRequest;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.domain.PdpPolicyViolation;
import pdp.policies.PolicyLoader;
import pdp.manage.ClassPathResourceManage;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static pdp.util.StreamUtils.singletonCollector;

@SuppressWarnings("unchecked")
public class PolicyIdpAccessEnforcerTest implements JsonMapper {

    private ClassPathResourceManage manage = new ClassPathResourceManage();
    private PolicyIdpAccessEnforcer subject = new PolicyIdpAccessEnforcer();
    private PdpPolicy pdpPolicy;

    private String uid = "uid";
    private String displayName = "John Doe";
    private String authenticatingAuthority = PolicyLoader.authenticatingAuthority;
    private String[] identityProviderIds = {authenticatingAuthority, "http://mock-idp2"};
    private String[] serviceProviderIds = {"http://mock-sp", "http://mock-sp2"};
    private String institutionId = "MOCK";
    private String notOwnedIdp = "http://not-owned-idp";
    private String notOwnedSp = "https://authz-admin.test2.surfconext.nl/shibboleth";

    @Before
    public void before() {
        this.pdpPolicy = new PdpPolicy("N/A", "pdpPolicyName", true, uid, authenticatingAuthority, displayName, true, "reg");
        //individual tests can overwrite this behaviour
        setupSecurityContext(true, entityMetadata(identityProviderIds), entityMetadata(serviceProviderIds));
        manage.allowAll(false);
    }

    private void setupSecurityContext(boolean policyIdpAccessEnforcement, Set<EntityMetaData> idpEntities, Set<EntityMetaData> spEntities) {
        SecurityContext context = new SecurityContextImpl();
        Authentication authentication = new PolicyIdpAccessAwareToken(
            new RunAsFederatedUser(uid, authenticatingAuthority, displayName, idpEntities, spEntities, EMPTY_LIST) {
                @Override
                public boolean isPolicyIdpAccessEnforcementRequired() {
                    return policyIdpAccessEnforcement;
                }
            });
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testActionAllowedHappyFlowNoIdps() throws Exception {
        this.subject.actionAllowed(pdpPolicy, PolicyAccess.WRITE, serviceProviderIds[0], EMPTY_LIST);
    }

    @Test
    public void testActionAllowedHappyFlowOwnedIdps() throws Exception {
        this.subject.actionAllowed(pdpPolicy, PolicyAccess.WRITE, serviceProviderIds[0], asList(identityProviderIds));
    }

    @Test(expected = PolicyIdpAccessMismatchServiceProviderException.class)
    public void testActionNotAllowedSpDoesNotMatch() throws Exception {
        setupSecurityContext(true, entityMetadata(identityProviderIds), entityMetadata("http://not-owned-sp"));
        this.subject.actionAllowed(pdpPolicy, PolicyAccess.WRITE, serviceProviderIds[0], EMPTY_LIST);
    }

    @Test(expected = PolicyIdpAccessMismatchIdentityProvidersException.class)
    public void testActionNotAllowedIdpsDoNotMatch() throws Exception {
        this.subject.actionAllowed(pdpPolicy, PolicyAccess.WRITE, serviceProviderIds[0], singletonList(notOwnedIdp));
    }

    @Test(expected = PolicyIdpAccessOriginatingIdentityProviderException.class)
    public void testActionNotAllowedWrongAuthenticatingAuthority() throws Exception {
        this.pdpPolicy.setAuthenticatingAuthority(notOwnedIdp);
        this.subject.actionAllowed(pdpPolicy, PolicyAccess.WRITE, serviceProviderIds[0], EMPTY_LIST);
    }

    @Test
    public void testActionNotAllowedDifferentAuthenticatingAuthorityButOwnerDifferentIdp() throws Exception {
        this.pdpPolicy.setAuthenticatingAuthority(notOwnedIdp);
        //we now do own notOwnedIdp
        setupSecurityContext(false, entityMetadata(identityProviderIds[0]), entityMetadata(notOwnedIdp));
        this.subject.actionAllowed(pdpPolicy, PolicyAccess.WRITE, serviceProviderIds[0], singletonList(identityProviderIds[0]));
    }

    @Test
    public void testActionNotAllowedButNoEnforcementForUser() throws Exception {
        setupSecurityContext(false, entityMetadata(identityProviderIds), entityMetadata(serviceProviderIds));
        this.subject.actionAllowed(pdpPolicy, PolicyAccess.WRITE, null, null);
    }

    @Test
    public void testActionAllowedViolations() throws Exception {
        this.subject.actionAllowed(null, PolicyAccess.VIOLATIONS, null, null);
    }

    @Test
    public void testActionAllowedIdpsAndSpAllowed() throws Exception {
        manage.allowAll(true);
        this.subject.actionAllowed(pdpPolicy, PolicyAccess.READ, notOwnedSp, Collections.EMPTY_LIST);
    }

    @Test
    public void filterViolations() throws Exception {
        List<PdpPolicyViolation> violations = pdpPolicyViolation(authenticatingAuthority, notOwnedIdp);
        Iterable<PdpPolicyViolation> filtered = this.subject.filterViolations(violations);

        //we expect exactly 1
        PdpPolicyViolation violation = stream(filtered.spliterator(), false).collect(singletonCollector());

        //we cheat. See PolicyIdpAccessEnforcerTest#pdpPolicyViolation
        assertEquals(authenticatingAuthority, violation.getResponse());
    }

    @Test
    public void filterViolationsNotEnforced() throws Exception {
        setupSecurityContext(false, entityMetadata(identityProviderIds), entityMetadata(serviceProviderIds));

        List<PdpPolicyViolation> violations = pdpPolicyViolation(authenticatingAuthority, notOwnedIdp);
        Iterable<PdpPolicyViolation> filtered = this.subject.filterViolations(violations);

        assertEquals(2, stream(filtered.spliterator(), false).count());
    }


    @Test
    public void filterPdpPolicies() throws Exception {
        PdpPolicyDefinition definition = new PdpPolicyDefinition();
        definition.setIdentityProviderIds(singletonList("http://idp.nl"));
        definition.setServiceProviderId("http://sp.nl");

        this.setupSecurityContext(true, emptySet(), entityMetadata("http://sp.nl"));

        List<PdpPolicyDefinition> policies = this.subject.filterPdpPolicies(singletonList(definition));
        assertTrue(policies.isEmpty());
    }

    @Test
    public void testAuthenticatingAuthority() throws Exception {
        assertEquals(authenticatingAuthority, subject.authenticatingAuthority());
    }

    @Test
    public void testUserIdentifier() throws Exception {
        assertEquals(uid, subject.username());
    }

    @Test
    public void testUserDisplayName() throws Exception {
        assertEquals(displayName, subject.userDisplayName());
    }

    private Set<EntityMetaData> entityMetadata(String... entityIds) {
        return asList(entityIds).stream().map(id -> new EntityMetaData(id, institutionId, null, null,
            true, true, new HashSet<String>())).collect(toSet());
    }

    private List<PdpPolicyViolation> pdpPolicyViolation(String... idpEntityIds) throws IOException {
        return asList(idpEntityIds).stream().map(idpEntityId -> new PdpPolicyViolation(new PdpPolicy(), jsonPolicyRequest(idpEntityId), idpEntityId, false)).collect(toList());
    }

    private String jsonPolicyRequest(String idpEntityId) {
        try {
            JsonPolicyRequest jsonPolicyRequest = objectMapper.readValue(new ClassPathResource("xacml/requests/base_request.json").getInputStream(), JsonPolicyRequest.class);
            jsonPolicyRequest.addOrReplaceResourceAttribute("IDPentityID", idpEntityId);
            return objectMapper.writeValueAsString(jsonPolicyRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}