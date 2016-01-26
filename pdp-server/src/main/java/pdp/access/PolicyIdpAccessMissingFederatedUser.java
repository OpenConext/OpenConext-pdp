package pdp.access;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = FORBIDDEN)
public class PolicyIdpAccessMissingFederatedUser extends AbstractPolicyIdpAccessException {

  public PolicyIdpAccessMissingFederatedUser(String msg) {
    super(msg);
  }

}
