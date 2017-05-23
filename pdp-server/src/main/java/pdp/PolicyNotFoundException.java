package pdp;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(String msg) {
        super(msg);
    }
}
