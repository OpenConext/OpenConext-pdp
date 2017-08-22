# OpenConext-pdp

[![Build Status](https://travis-ci.org/OpenConext/OpenConext-pdp.svg)](https://travis-ci.org/OpenConext/OpenConext-pdp)
[![codecov.io](https://codecov.io/github/OpenConext/OpenConext-pdp/coverage.svg)](https://codecov.io/github/OpenConext/OpenConext-pdp)

OpenConext implementation of a XACML based PDP engine for access policy enforcement including a GUI for maintaining policies

## Getting started

### System Requirements

- Java 8
- Maven 3
- MySQL 5.5+
- npm 
- NodeJS 7.10.0 (best managed with `nvm`, current version in [.node-version](dashboard/.node-version)

### Create database

Connect to your local mysql database: `mysql -uroot`

Execute the following:

```sql
CREATE DATABASE pdpserver DEFAULT CHARACTER SET latin1;
create user 'root'@'localhost';
grant all on pdpserver.* to 'root'@'localhost';
```

## Building and running

### The pdp-server

This project uses Spring Boot and Maven. To run locally, type:

    cd pdp-server

    mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=dev"

When developing, it's convenient to just execute the applications main-method, which is in [PdpApplication](pdp-server/src/main/java/pdp/PdpApplication.java). Don't forget
to set the active profile to dev otherwise the application uses the real VOOT client on the test environment.

### The pdp-gui

    cd pdp-gui

Initial setup if you do:

    npm install

Add new dependencies to `devDependencies`:

    npm install ${dep} --dev

To build:

    npm run webpack

To run locally:

    npm run local

Browse to the [application homepage](http://localhost:8001/).

## Testing

There are (slow) integration tests for PdpApplication where various decisions are tested against a full-blown running Spring app. See [PdpEngineTest](pdp-server/src/test/java/pdp/PdpEngineTest.java)

If you want to test individual Policies with specific Request / Response JSON then use the (much faster) [StandAlonePdpEngineTest](pdp-server/src/test/java/pdp/StandAlonePdpEngineTest.java)

If you want to test policies against a full test system (e.g. the VM) then you can use the Mujina API to add or reset attributes:

    curl -v -H "Accept: application/json" -H "Content-type: application/json" -X PUT -d '{"value": ["hero"]}' "https://mujina-idp.vm.openconext.org/api/attributes/urn:mace:dir:attribute-def:eduPersonAffiliation"
    curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST "https://mujina-idp.vm.openconext.org/api/reset"

## Miscellaneous

### Design considerations

The XACML framework works with policies defined in XML. We store the policies as XML strings in the database. However to
effectively let XACML evaluate policies we need to convert them to the internal XACML format - see [OpenConextEvaluationContextFactory](pdp-server/src/main/java/pdp/xacml/OpenConextEvaluationContextFactory.java).

Working with XML on the pdp-gui does not work well and we want to keep the pdp-gui simple. Therefore the PdpPolicyDefinition is used as an
intermediate format for policies that is easy to work with for the pdp-gui and also enables the server to transform
it easily into the desired - yet very complex - XML format.

Using the internal XACML Policy class hierarchy for communication back and forth with the client was not an option because
of the cyclic dependencies in the hierarchy (and not desirable because of the complexity it would have caused).

### Architecture

See [this image](https://raw.githubusercontent.com/OpenConext/OpenConext-pdp/master/pdp-gui/src/images/authz_poc.001.png)

### Security

Read this [section](Security.md) for a in-depth security overview.

### Policy limitations

The policies that can be created are limited in functionality:

* All string comparisons are `urn:oasis:names:tc:xacml:1.0:function:string-equal`
* Every EngineBlock policy has exactly one Permit rule and one Deny rule
* Every Stepup policy has multiple Permits rules and no Deny rule
* The target of the policy is limited to exactly one SPENtityID and zero or more IDPEntityIDs
* A EngineBlock policy is either a Deny policy or a Permit policy
* A Stepup policy is always a Permit policy with one final / last Permit without any obligation
* All policies have a RuleCombiningAlgId of `urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable`
* All attributes with the same name are treated with the logical OR operator
* All attributes with a different name are exclusively treated with the logical OR or AND operator depending on the type of policy
* Rule targets if not empty only can contain the attributes of the category `urn:oasis:names:tc:xacml:1.0:subject-category:access-subject`
* The combining set of policies has the policy combining algorithm `urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides`

### Policy access

The Admin GUI has no restrictions in the accessibility of policies. The external API for trusted applications restricts access to policies based on the Identity Provider
and the possible associated Service Provider(s) of the user and the corresponding Service and Identity Provider(s) of the policy. See
 [this image](https://raw.githubusercontent.com/OpenConext/OpenConext-pdp/master/pdp-gui/src/images/PdP_policies_access.001.jpeg) for an overview of the logic applied in determining accessibility.

### Local database content

We don't provide flyway migrations to load initial policies. 

However if you start up the application with the spring.profiles.active=dev then all the policies
in the folder `OpenConext-pdp/pdp-server/src/main/resources/xacml/policies` are added to the database. Do note that any other policies already in the database are deleted.

### Service Registry

The pdp-server needs to access the metadata of Identity and Service providers from the Service Registry. In production modus the content is read (and periodically refreshed)
from the API exposed by the [metadata provider](https://github.com/OpenConext/OpenConext-metadata-exporter):

* https://multidata.surfconext.nl/service-providers.json
* https://multidata.surfconext.nl/identity-providers.json

In any other modus the content is read from the file system:

* [saml20-idp-remote.json](pdp-server/src/main/resources/service-registry/saml20-idp-remote.json)
* [saml20-sp-remote.json](pdp-server/src/main/resources/service-registry/saml20-sp-remote.json)

To sync the data of the file system with the test data of `https://multidata.test.surfconext.nl` run the [refreshEntityMetadata](pdp-server/scripts/refreshEntityMetadata.sh)
script.

### Configuration and Deployment

On its classpath, the application has an [application.properties](pdp-server/src/main/resources/application.properties) file that
contains configuration defaults that are convenient when developing.

When the application actually gets deployed to a meaningful platform, it is pre-provisioned with ansible and the application.properties depends on
environment specific properties in the group_vars. See the project OpenConext-deploy and the role pdp for more information.

For details, see the [Spring Boot manual](http://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/).

### cUrl

The API can be tested with cUrl. See [WebSecurityConfig](pdp-server/src/main/java/pdp/WebSecurityConfig.java) for the security rules. When starting in dev modus
the mock Shib headers are added automatically.

```
curl -ik -H "Content-Type: application/json" http://localhost:8080/pdp/api/internal/policies
curl -ik --user "pdp_admin:secret" -H "Content-Type: application/json" -H "X-IDP-ENTITY-ID: http://mock-idp" -H "X-UNSPECIFIED-NAME-ID: test" -H "X-DISPLAY-NAME: okke" http://localhost:8080/pdp/api/protected/policies
curl -ik -X POST --data-binary @./src/test/resources/xacml/requests/test_request_sab_policy.json --user "pdp_admin:secret" -H "Content-Type: application/json" http://localhost:8080/pdp/api/decide/policy
```

### OpenAZ dependency

The OpenConext-pdp project heavily uses the PD framework https://github.com/apache/incubator-openaz. This repo is cloned in 
https://github.com/OpenConext/incubator-openaz-openconext and changes - e.g. distribution management, some bug fixes and minor optimizations - are
pushed to openconext/develop branch in https://github.com/OpenConext/incubator-openaz-openconext.

To pull in changes from upstream run `./git-fetch-upstream.sh`
