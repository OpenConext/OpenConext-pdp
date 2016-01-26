package pdp.access;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@SuppressWarnings("serial")
@ResponseStatus(value = FORBIDDEN)
public class PolicyIdpAccessMismatchIdentityProvidersException extends AbstractPolicyIdpAccessException {

  public PolicyIdpAccessMismatchIdentityProvidersException(String msg) {
    super(msg);
  }
}
