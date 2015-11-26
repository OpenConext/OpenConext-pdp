package pdp.security;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicy;
import pdp.shibboleth.ShibbolethUser;

import java.util.Arrays;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class PolicyIdpAccessEnforcerTest {

  private PolicyIdpAccessEnforcer subject;
  private PdpPolicy pdpPolicy;

  private String pdpPolicyName = "pdpPolicyName";
  private String uid = "uid";
  private String authenticatingAuthority = "http://mock-idp";
  private String[] identityProviderIds = {"http://mock-idp", "http://mock-idp2"};
  private String[] serviceProviderIds = {"http://mock-sp", "http://mock-sp2"};
  private String institutionId = "MOCK";
  private String notOwnedIdp = "http://not-owned-idp";

  @Before
  public void before() {
    this.subject = new PolicyIdpAccessEnforcer(true);
    this.pdpPolicy = new PdpPolicy("N/A", pdpPolicyName, uid, authenticatingAuthority);
    //individual tests can overwrite this behaviour
    setupSecurityContext(true, entityMetadata(identityProviderIds), entityMetadata(serviceProviderIds));
  }

  private void setupSecurityContext(boolean policyIdpAccessEnforcement, Set<EntityMetaData> idpEntities, Set<EntityMetaData> spEntities) {
    SecurityContext context = new SecurityContextImpl();
    Authentication authentication = new TestingAuthenticationToken(
        new ShibbolethUser(
            uid,
            authenticatingAuthority,
            "N/A",
            idpEntities,
            spEntities,
            EMPTY_LIST,
            policyIdpAccessEnforcement),
        "N/A");
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }

  @Test
  public void testActionAllowedHappyFlowNoIdps() throws Exception {
    this.subject.actionAllowed(pdpPolicy, serviceProviderIds[0], EMPTY_LIST);
  }

  @Test
  public void testActionAllowedHappyFlowOwnedIdps() throws Exception {
    this.subject.actionAllowed(pdpPolicy, serviceProviderIds[0], Arrays.asList(identityProviderIds));
  }

  @Test(expected = PolicyIdpAccessMismatchServiceProviderException.class)
  public void testActionNotAllowedSpDoesNotMatch() throws Exception {
    setupSecurityContext(true, entityMetadata(identityProviderIds), entityMetadata("http://not-owned-sp"));
    this.subject.actionAllowed(pdpPolicy, serviceProviderIds[0], EMPTY_LIST);
  }

  @Test(expected = PolicyIdpAccessMismatchIdentityProvidersException.class)
  public void testActionNotAllowedIdpsDoNotMatch() throws Exception {
    this.subject.actionAllowed(pdpPolicy, serviceProviderIds[0], singletonList(notOwnedIdp));
  }

  @Test(expected = PolicyIdpAccessOriginatingIdentityProviderException.class)
  public void testActionNotAllowedWrongAuthenticatingAuthority() throws Exception {
    this.pdpPolicy.setAuthenticatingAuthority(notOwnedIdp);
    this.subject.actionAllowed(pdpPolicy, serviceProviderIds[0], EMPTY_LIST);
  }

  @Test
  public void testActionNotAllowedDifferentAuthenticatingAuthorityButOwnerDifferentIdp() throws Exception {
    this.pdpPolicy.setAuthenticatingAuthority("http://different-idp");
    setupSecurityContext(false, entityMetadata(identityProviderIds[0]), entityMetadata("http://different-idp"));
    this.subject.actionAllowed(pdpPolicy, serviceProviderIds[0], singletonList(identityProviderIds[0]));
  }

  @Test
  public void testActionNotAllowedButNoEnforcementForUser() throws Exception {
    setupSecurityContext(false, entityMetadata(identityProviderIds), entityMetadata(serviceProviderIds));
    this.subject.actionAllowed(pdpPolicy, null, null);
  }

  @Test
  public void testActionNotAllowedButNoGlobalEnforcement() throws Exception {
    this.subject = new PolicyIdpAccessEnforcer(false);
    this.subject.actionAllowed(pdpPolicy, null, null);
  }

  @Test
  public void testAuthenticatingAuthority() throws Exception {
    assertEquals(authenticatingAuthority, subject.authenticatingAuthority());
  }

  @Test
  public void testUserIdentifier() throws Exception {
    assertEquals(uid, subject.username());
  }

  private Set<EntityMetaData> entityMetadata(String... entityIds) {
    return asList(entityIds).stream().map(id -> new EntityMetaData(id, institutionId, null, null, null, null)).collect(toSet());
  }

}