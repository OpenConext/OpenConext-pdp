package pdp.access;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@SuppressWarnings("serial")
@ResponseStatus(value = FORBIDDEN)
public class PolicyIdpAccessMissingFederatedUser extends AbstractPolicyIdpAccessException {

  public PolicyIdpAccessMissingFederatedUser(String msg) {
    super(msg);
  }

}
