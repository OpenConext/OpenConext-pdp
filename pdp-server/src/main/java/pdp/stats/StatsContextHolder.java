package pdp.stats;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.util.Assert;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * We need to keep track of response times, but we do not have the correct hooks in the XACML lib. So
 * we use ThreadLocal to store stats and clear and instantiate them here
 */
public class StatsContextHolder implements ServletRequestListener {

  private static final ThreadLocal<StatsContext> contextHolder = new ThreadLocal<>();

  @Override
  public void requestInitialized(ServletRequestEvent sre) {
    contextHolder.set(new StatsContext());
  }

  @Override
  public void requestDestroyed(ServletRequestEvent sre) {
    contextHolder.remove();
  }


  public void clearContext() {
    contextHolder.remove();
  }

  public StatsContext getContext() {
    StatsContext ctx = contextHolder.get();
    if (ctx == null) {
      contextHolder.set(new StatsContext());
    }
    return ctx;
  }

  public void setContext(StatsContext context) {
    Assert.notNull(context, "Only non-null StatsContext instances are permitted");
    contextHolder.set(context);
  }

}
