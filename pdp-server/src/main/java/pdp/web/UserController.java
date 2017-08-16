package pdp.web;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pdp.access.FederatedUser;

import java.util.Collections;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class UserController {

    @RequestMapping(method = GET, value = "internal/users/me")
    public FederatedUser user() {
        return (FederatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/internal/users/ping")
    public Map<String, String> ping() {
        return Collections.singletonMap("Ping", "Ok");
    }
}
