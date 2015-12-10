package pdp.access;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class PolicyIdpAccessMismatchServiceProviderException extends AbstractPolicyIdpAccessException {

  public PolicyIdpAccessMismatchServiceProviderException(String msg) {
    super(msg);
  }
}
