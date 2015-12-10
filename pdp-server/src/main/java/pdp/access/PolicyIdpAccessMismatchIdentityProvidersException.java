package pdp.access;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class PolicyIdpAccessMismatchIdentityProvidersException extends AbstractPolicyIdpAccessException {

  public PolicyIdpAccessMismatchIdentityProvidersException(String msg) {
    super(msg);
  }
}
