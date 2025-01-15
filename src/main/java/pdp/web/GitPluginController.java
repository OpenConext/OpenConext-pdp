package pdp.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

@RestController
public class GitPluginController {

    @RequestMapping(method = RequestMethod.GET, value = "/public/git")
    public Properties git() throws IOException {
        Properties props = new Properties();
        props.load(new ClassPathResource("git.properties").getInputStream());
        return props;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/health")
    public void health(HttpServletRequest request, HttpServletResponse respsonse) throws ServletException, IOException {
        request.getRequestDispatcher("/health").forward(request, respsonse);
    }


}
