package pdp.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private final ErrorAttributes errorAttributes;

    public ErrorController() {
        this.errorAttributes = new DefaultErrorAttributes();
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        ServletWebRequest webRequest = new ServletWebRequest(request);

        Map<String, Object> result = errorAttributes.getErrorAttributes(webRequest,
            ErrorAttributeOptions.of(
                ErrorAttributeOptions.Include.EXCEPTION,
                ErrorAttributeOptions.Include.MESSAGE));

        Throwable error = errorAttributes.getError(webRequest);
        HttpStatus statusCode;
        if (error == null) {
            Object status = result.get("status");
            statusCode = status != null && !Integer.valueOf(999).equals(status) ? HttpStatus.valueOf((Integer) status) : INTERNAL_SERVER_ERROR;
        } else {
            //https://github.com/spring-projects/spring-boot/issues/3057
            ResponseStatus annotation = AnnotationUtils.getAnnotation(error.getClass(), ResponseStatus.class);
            statusCode = annotation != null ? annotation.value() : INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<>(result, statusCode);
    }

}
