package pdp.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pdp.domain.EntityMetaData;
import pdp.serviceregistry.ServiceRegistry;

import java.util.List;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class ServiceRegistryController {

  private final ServiceRegistry serviceRegistry;

  @Autowired
  public ServiceRegistryController(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/internal/serviceProviders")
  public List<EntityMetaData> serviceProviders() {
    return serviceRegistry.serviceProviders();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/internal/identityProviders")
  public List<EntityMetaData> identityProviders() {
    return serviceRegistry.identityProviders();
  }

}
