package pdp.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdp.JsonMapper;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

/**
 * We need to keep track of response times, but we do not have the correct hooks in the XACML lib. So
 * we use ThreadLocal to store stats and clear and instantiate them here
 */
public class StatsContextHolder implements ServletRequestListener, JsonMapper {

    private static final ThreadLocal<StatsContext> contextHolder = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger("analytics");

    private final String path;


    public StatsContextHolder(String path) {
        this.path = path;
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
            logger.info(objectMapper.writeValueAsString(context));
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
