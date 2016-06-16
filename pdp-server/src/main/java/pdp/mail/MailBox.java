package pdp.mail;

import pdp.domain.PdpPolicyDefinition;

import java.util.List;
import java.util.Map;

public interface MailBox {

  void sendConflictsMail(Map<String, List<PdpPolicyDefinition>> conflicts) ;

}
