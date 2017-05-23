package pdp.shibboleth.mock;


import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.list;
import static org.junit.Assert.assertEquals;
import static pdp.access.FederatedUserBuilder.UID_HEADER_NAME;

public class MockShibbolethFilterTest {

    private MockShibbolethFilter subject = new MockShibbolethFilter();

    @Test
    public void testDoFilter() throws Exception {
        MockFilterChain filterChain = new MockFilterChain();
        subject.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);
        HttpServletRequest request = (HttpServletRequest) filterChain.getRequest();
        assertEquals("urn:collab:person:example.com:admin", request.getHeader(UID_HEADER_NAME));

        assertEquals(3, list(request.getHeaderNames()).size());

    }
}