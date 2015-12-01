package pdp.web;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CsrfProtectionMatcherTest {

  private String baseUrl = "https://pdp.surfconext.nl/";
  private CsrfProtectionMatcher subject = new CsrfProtectionMatcher();

  @Test
  public void testMatches() throws Exception {
    assertTrue(subject.matches(getMockHttpServletRequest("PUT", "/internal")));
    assertTrue(subject.matches(getMockHttpServletRequest("ANY_OTHER", "/internal")));

    assertFalse(subject.matches(getMockHttpServletRequest("GET", "/internal")));
    assertFalse(subject.matches(getMockHttpServletRequest("HEAD", "/internal")));
    assertFalse(subject.matches(getMockHttpServletRequest("TRACE", "/internal")));
    assertFalse(subject.matches(getMockHttpServletRequest("OPTIONS", "/internal")));

    assertFalse(subject.matches(getMockHttpServletRequest("ANY_OTHER", "/not-internal")));
  }

  private MockHttpServletRequest getMockHttpServletRequest(String method, String servletPath) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, baseUrl);
    request.setServletPath(servletPath);
    return request;
  }
}