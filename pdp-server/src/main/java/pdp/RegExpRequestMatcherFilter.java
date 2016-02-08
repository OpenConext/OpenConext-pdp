package pdp;

import org.springframework.util.Assert;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegExpRequestMatcherFilter implements Filter {

  private final Filter filter;
  private final Pattern pattern;

  public RegExpRequestMatcherFilter(Filter filter, String regExp) {
    Assert.notNull(filter);
    Assert.notNull(regExp);

    this.filter = filter;
    this.pattern = Pattern.compile(regExp);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    filter.init(filterConfig);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String url = getRequestPath((HttpServletRequest) request);
    if (pattern.matcher(url).matches()) {
      filter.doFilter(request, response, chain);
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    filter.destroy();
  }

  private String getRequestPath(HttpServletRequest request) {
    String url = request.getServletPath();
    if (request.getPathInfo() != null) {
      url += request.getPathInfo();
    }
    return url;
  }

}
