package pdp;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PdpPolicyException extends RuntimeException {

    private final Map<String, String> details;

    public PdpPolicyException(String... details) {
        super("Bad Request");
        if (details.length % 2 != 0) {
            throw new RuntimeException("Can not transform non even vararg of messages to errorMap");
        }
        Map<String, String> errorMap = new HashMap<>();
        for (int i = 0; i < details.length; i = i + 2) {
            errorMap.put(details[i], details[i + 1]);
        }
        this.details = errorMap;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
