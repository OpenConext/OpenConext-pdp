package pdp.access;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class PolicyIdpAccessOriginatingIdentityProviderException extends AbstractPolicyIdpAccessException {

  public PolicyIdpAccessOriginatingIdentityProviderException(String msg) {
    super(msg);
  }
}
