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

  private static final ThreadLocal<StatsContext> contextHolder = new ThreadLocal<StatsContext>();

  @Override
  public void requestDestroyed(ServletRequestEvent sre) {
    System.out.println("requestDestroyed");
  }

  @Override
  public void requestInitialized(ServletRequestEvent sre) {
    System.out.println("requestInitialized");

  }

  public void clearContext() {
    contextHolder.remove();
  }

  public StatsContext getContext() {
    StatsContext ctx = contextHolder.get();
    if (ctx == null) {
      ctx = new StatsContext();
      contextHolder.set(ctx);
    }
    return ctx;
  }

  public void setContext(StatsContext context) {
    Assert.notNull(context, "Only non-null StatsContext instances are permitted");
    contextHolder.set(context);
  }

}
