package pdp.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pdp.access.PolicyIdpAccessEnforcer;
import pdp.domain.EntityMetaData;
import pdp.serviceregistry.ServiceRegistry;

import java.util.List;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class ServiceRegistryController {

    private final ServiceRegistry serviceRegistry;
    private final PolicyIdpAccessEnforcer policyIdpAccessEnforcer;

    @Autowired
    public ServiceRegistryController(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.policyIdpAccessEnforcer = new PolicyIdpAccessEnforcer(serviceRegistry);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/serviceProviders")
    public List<EntityMetaData> serviceProviders() {
        return serviceRegistry.serviceProviders();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/identityProviders")
    public List<EntityMetaData> identityProviders() {
        return serviceRegistry.identityProviders();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/identityProviders/scoped")
    public List<EntityMetaData> identityProvidersScoped() {
        return policyIdpAccessEnforcer.filterIdentityProviders(serviceRegistry.identityProviders());
    }
}
