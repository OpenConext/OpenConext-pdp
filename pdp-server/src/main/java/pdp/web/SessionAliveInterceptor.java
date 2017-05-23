package pdp.web;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionAliveInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        // add this header as an indication to the JS-client that this is a regular, non-session-expired response.
        response.addHeader("X-SESSION-ALIVE", "true");
        return true;
    }

}
