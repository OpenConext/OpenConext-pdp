# OpenConext-pdp

OpenConext implementation of a XACML based PDP engine for access policy enforcement including a GUI for maintaining policies

## Getting started

### System Requirements

- Java 8
- Maven 3
- MySQL 5.5
- Gruntjs

### Create database

Connect to your local mysql database: `mysql -uroot`

Execute the following:

```sql
CREATE DATABASE `pdp-server` DEFAULT CHARACTER SET latin1;
create user 'pdp-serverrw'@'localhost' identified by 'secret';
grant all on `pdp-server`.* to 'pdp-serverrw'@'localhost';
```

## Building and running

### The pdp-server

This project uses Spring Boot and Maven. To run locally, type:

`cd pdp-server`
`mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=dev"`

When developing, it's convenient to just execute the applications main-method, which is in [PdpApplication](pdp-server/src/main/java/pdp/PdpApplication.java). Don't forget
to set the active profile to dev otherwise the application uses the real VOOT client on the test environment.

### The pdp-gui

The client is build with react.js and to get initially started:

`cd pdp-gui`

`brew install npm;`
`gem install sass;`
`gem install sass-globbing;`
`gem install compass;`
`npm install -g grunt-cli;`
`npm install;`

When new grunt dependencies are added:

`npm install`

To build:

`grunt watch`

To run locally:

`grunt server`

When you browse to the [application homepage](http://localhost:8001/) you will be prompted for a login. Anything - for now - is accepted.

## Miscellaneous

### Design considerations

The XACML framework works with policies defined in XML. We store the policies as XML strings in the database. However to
effectively let XACML evaluate policies we need to convert them to the internal XACML format - see [OpenConextEvaluationContextFactory](pdp-server/src/main/java/pdp/xacml/OpenConextEvaluationContextFactory.java).

Working with XML on the pdp-gui does not work well and we want to keep the pdp-gui simple. Therefore the PdpPolicyDefinition is used as an
intermediate format for policies that is easy to work with for the pdp-gui and also enables the server to transform
it easily into the desired - yet very complex - XML format.

Using the internal XACML Policy class hierarchy for communication back and forth with the client was not an option because
of the cyclic dependencies in the hierarchy (and not desirable because of the complexity it would have caused).

### Local database content

We don't provide flyway migrations to load initial policies. You need to work with the GUI to define and store them. However to test locally against
a database with policies you can load the same policies used in testing with the following command

`mysql -u root pdp-server < .pdp-server/src/test/resources/sql/policies.sql`

### Testing

There are integration tests for PdpApplication that tests the various decisions against a running Spring app. See [PdpApplicationTest](pdp-server/src/test/java/pdp/PdpApplicationTest.java)

One can also use cUrl to test against a running Spring application but you will need to load the policies as described in the previous step. 

Start the server and go the directory src/test/resources. Use the following command to test the permit decision:

`curl -i --user pdp_admin:secret -X POST --header "Content-Type: application/json" -d @./src/test/resources/SURFspotAccess.Permit.CategoriesShorthand.json http://localhost:8080/decide`

The directory src/test/resources contains additional test JSON inputs. To test against the test2 environment change the endpoint to `https://pdp.test2.surfconext.nl/decide`. 

Examples:

`curl -i --user pdp_admin:secret -X POST --header "Content-Type: application/json" -d @./src/test/resources/TeamAccess.Permit.json https://pdp.test2.surfconext.nl/decide`

`curl -i --user pdp_admin:secret -X POST --header "Content-Type: application/json" -d @./src/test/resources/SURFspotAccess.Deny.json https://pdp.test2.surfconext.nl/decide`

### Configuration and Deployment

On its classpath, the application has an [application.properties](src/main/resources/application.properties) file that
contains configuration defaults that are convenient when developing.

When the application actually gets deployed to a meaningful platform, it is pre-provisioned with ansible and the application.properties depends on
environment specific properties in the group_vars. See the project OpenConext-deploy and the role pdp for more information.

For details, see the [Spring Boot manual](http://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/).

