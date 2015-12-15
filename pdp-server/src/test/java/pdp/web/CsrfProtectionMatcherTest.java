package pdp.web;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static pdp.access.FederatedUserBuilder.*;

public class CsrfProtectionMatcherTest {

  private String baseUrl = "https://pdp.surfconext.nl/";

  private CsrfProtectionMatcher subject = new CsrfProtectionMatcher();

  @Test
  public void testMatches() throws Exception {
    assertTrue(subject.matches(getMockHttpServletRequest("PUT", "/internal")));
    assertTrue(subject.matches(getMockHttpServletRequest("ANY_OTHER", "/internal")));

    assertTrue(subject.matches(getMockHttpServletRequest("ANY_OTHER", "/internal", X_IDP_ENTITY_ID, X_UNSPECIFIED_NAME_ID)));

    assertFalse(subject.matches(getMockHttpServletRequest("PUT", "/internal", X_IDP_ENTITY_ID, X_UNSPECIFIED_NAME_ID, X_DISPLAY_NAME)));
    assertFalse(subject.matches(getMockHttpServletRequest("ANY_OTHER", "/internal", X_IDP_ENTITY_ID, X_UNSPECIFIED_NAME_ID, X_DISPLAY_NAME)));

    assertFalse(subject.matches(getMockHttpServletRequest("GET", "/internal")));
    assertFalse(subject.matches(getMockHttpServletRequest("HEAD", "/internal")));
    assertFalse(subject.matches(getMockHttpServletRequest("TRACE", "/internal")));
    assertFalse(subject.matches(getMockHttpServletRequest("OPTIONS", "/internal")));

    assertFalse(subject.matches(getMockHttpServletRequest("ANY_OTHER", "/not-internal")));
  }

  private MockHttpServletRequest getMockHttpServletRequest(String method, String servletPath, String... headers) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, baseUrl);
    request.setServletPath(servletPath);
    asList(headers).forEach(header -> request.addHeader(header, "does_not_matter"));
    return request;
  }
}