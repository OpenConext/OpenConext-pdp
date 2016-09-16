## Security

The main endpoint is the [PdpController](pdp-server/src/main/java/pdp/web/PdpController.java). It is protected
by the security configuration in [WebSecurityConfig](pdp-server/src/main/java/pdp/WebSecurityConfig.java).

There are two [AuthenticationProviders](http://docs.spring.io/spring-security/site/docs/current/reference/html/jc.html#jc-authentication-authenticationprovider) in
the PDP application:

1. A [PreAuthenticatedAuthenticationProvider](http://docs.spring.io/spring-security/site/docs/current/reference/html/preauth.html#preauth)
implementation which protects all the ```/internal/**``` endpoints.
2. A [BasicAuthenticationFilter](pdp-server/src/main/java/pdp/access/BasicAuthenticationProvider.java) which protects all the
`/protected/**` endpoints.

### Shibboleth
The [Shibboleth filter](pdp-server/src/main/java/pdp/shibboleth/ShibbolethPreAuthenticatedProcessingFilter.java) inspects
the request headers for pre-populated Shibboleth values. The task of building a Shibboleth federated user based on the
request headers is handled by the [FederatedUserBuilder](https://github.com/OpenConext/OpenConext-pdp/blob/master/pdp-server/src/main/java/pdp/access/FederatedUserBuilder.java#L63).

Only federated logged in users are therefore allowed access to the ```/internal/**``` endpoints. The ```/internal/**```endpoint
are used by the internal PDP JavaScript GUI. It is highly recommended to limit access to the PDP Admin GUI using a PDP policy.

### Basic Authentication
There are two trusted clients that use the username/password `/protected/**` endpoints:

1. EngineBlock for the policy decision endpoint
2. Dashboard server for the maintenance of institutional policies.

The Dashboard server sends custom request headers to PDP API calls indicating who is logged in. The PDP application uses
these custom headers to populate an [institutional admin](https://github.com/OpenConext/OpenConext-pdp/blob/master/pdp-server/src/main/java/pdp/access/FederatedUserBuilder.java#L44).

Whenever policies are returned, created or updated the PDPController asks the (PolicyIdpAccessEnforcer)[pdp-server/src/main/java/pdp/access/PolicyIdpAccessEnforcer.java]
to check if the action is allowed. Therefore institutional admins in Dashboard can only see their own policies.

#### Policy access

The internal PDP admin GUI has no restrictions in the accessibility of policies, because the `/internal/**` endpoints
are not limited by the (PolicyIdpAccessEnforcer)[pdp-server/src/main/java/pdp/access/PolicyIdpAccessEnforcer.java].

The external API for trusted applications - e.g. Dashboard server - restricts access to policies based on the Identity
Provider and the possible associated Service Provider(s) of the user and the corresponding Service and Identity Provider(s)
of the policy.

See [this image](https://raw.githubusercontent.com/OpenConext/OpenConext-pdp/master/pdp-gui/src/images/PdP_policies_access.001.jpeg)
for an overview of the logic applied in determining policy accessibility.



