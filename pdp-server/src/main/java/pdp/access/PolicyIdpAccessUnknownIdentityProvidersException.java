package pdp.access;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SuppressWarnings("serial")
@ResponseStatus(value = BAD_REQUEST)
public class PolicyIdpAccessUnknownIdentityProvidersException extends AbstractPolicyIdpAccessException {

  public PolicyIdpAccessUnknownIdentityProvidersException(String msg) {
    super(msg);
  }
}
