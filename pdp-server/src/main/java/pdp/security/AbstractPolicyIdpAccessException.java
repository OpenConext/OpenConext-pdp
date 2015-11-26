package pdp.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class AbstractPolicyIdpAccessException extends RuntimeException {

  public AbstractPolicyIdpAccessException(String msg) {
    super(msg);
  }
}
