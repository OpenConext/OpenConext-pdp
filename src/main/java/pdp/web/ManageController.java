package pdp.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pdp.access.PolicyIdpAccessEnforcer;
import pdp.domain.EntityMetaData;
import pdp.manage.Manage;

import java.util.List;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class ManageController {

    private final Manage manage;
    private final PolicyIdpAccessEnforcer policyIdpAccessEnforcer;

    @Autowired
    public ManageController(Manage manage) {
        this.manage = manage;
        this.policyIdpAccessEnforcer = new PolicyIdpAccessEnforcer();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/serviceProviders")
    public List<EntityMetaData> serviceProviders() {
        return manage.serviceProviders();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/identityProviders")
    public List<EntityMetaData> identityProviders() {
        return manage.identityProviders();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/identityProviders/scoped")
    public List<EntityMetaData> identityProvidersScoped() {
        return policyIdpAccessEnforcer.filterIdentityProviders(manage.identityProviders());
    }
}
