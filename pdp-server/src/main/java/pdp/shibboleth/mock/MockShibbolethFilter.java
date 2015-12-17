package pdp.shibboleth.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;
import pdp.policies.PolicyLoader;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import static pdp.access.FederatedUserBuilder.*;

public class MockShibbolethFilter extends GenericFilterBean {

  private static final Logger LOG = LoggerFactory.getLogger(MockShibbolethFilter.class);

  public MockShibbolethFilter() {
    LOG.info("MockShibbolethFilter initializing...");
  }

  private static class SetHeader extends HttpServletRequestWrapper {

    private final HashMap<String, String> headers;

    public SetHeader(HttpServletRequest request) {
      super(request);
      this.headers = new HashMap<>();
    }

    public void setHeader(String name, String value) {
      this.headers.put(name, value);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      List<String> names = Collections.list(super.getHeaderNames());
      names.addAll(headers.keySet());
      return Collections.enumeration(names);
    }

    @Override
    public String getHeader(String name) {
      if (headers.containsKey(name)) {
        return headers.get(name);
      }
      return super.getHeader(name);
    }
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    SetHeader wrapper = new SetHeader((HttpServletRequest) servletRequest);
    wrapper.setHeader(UID_HEADER_NAME, "urn:collab:person:example.com:admin");
    wrapper.setHeader(SHIB_AUTHENTICATING_AUTHORITY, PolicyLoader.authenticatingAuthority);
    wrapper.setHeader(DISPLAY_NAME_HEADER_NAME, "John Doe");

    filterChain.doFilter(wrapper, servletResponse);
  }
}
