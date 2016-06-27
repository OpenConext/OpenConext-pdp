package pdp.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import pdp.JsonMapper;
import pdp.domain.PdpDecision;
import pdp.repositories.PdpDecisionRepository;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

/**
 * We need to keep track of response times, but we do not have the correct hooks in the XACML lib. So
 * we use ThreadLocal to store stats and clear and instantiate them here
 */
public class StatsContextHolder implements ServletRequestListener, JsonMapper {

  private static final ThreadLocal<StatsContext> contextHolder = new ThreadLocal<>();

  private final String path;
  private final PdpDecisionRepository decisionRepository;

  public StatsContextHolder(String path, PdpDecisionRepository decisionRepository) {
    this.path = path;
    this.decisionRepository = decisionRepository;
  }

  @Override
  public void requestInitialized(ServletRequestEvent sre) {
    if (applyForPath(sre)) {
      contextHolder.set(new StatsContext());
    }
  }

  @Override
  public void requestDestroyed(ServletRequestEvent sre) {
    if (applyForPath(sre)) {
      StatsContext context = contextHolder.get();
      new Thread(() -> saveContext(context)).start();
      contextHolder.remove();
    }
  }

  private void saveContext(StatsContext context) {
    if (context.getServiceProvicer() == null) {
      return;
    }
    try {
      PdpDecision pdpDecision = new PdpDecision();
      pdpDecision.setDecisionJson(objectMapper.writeValueAsString(context));
      decisionRepository.save(pdpDecision);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean applyForPath(ServletRequestEvent sre) {
    return ((HttpServletRequest) sre.getServletRequest()).getRequestURI().endsWith(path);
  }

  public static StatsContext getContext() {
    StatsContext ctx = contextHolder.get();
    if (ctx == null) {
      contextHolder.set(new StatsContext());
    }
    return contextHolder.get();
  }
}
