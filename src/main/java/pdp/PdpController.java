package pdp;

import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.std.json.JSONRequest;
import org.apache.openaz.xacml.std.json.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdpController {

  private static Logger LOG = LoggerFactory.getLogger(PdpController.class);

  @Autowired
  private PDPEngine pdpEngine;

  @RequestMapping(method = RequestMethod.POST, headers = {"content-type=application/json"}, value = "/decide")
  public String myGroups(@RequestBody String payload) throws Exception {
    LOG.debug("decide request: {}", payload);

    Request pdpRequest = JSONRequest.load(payload);
    Response pdpResponse = pdpEngine.decide(pdpRequest);
    String response = JSONResponse.toString(pdpResponse, LOG.isDebugEnabled());

    LOG.debug("decide response: {}", response);

    return response;

  }

}
