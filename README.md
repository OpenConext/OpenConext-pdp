# OpenConext-pdp
OpenConext implementation of a XACML based PDP engine for access policy enforcement

# Create database

Connect to your local mysql database: `mysql -uroot`

Execute the following:

```sql
CREATE DATABASE `pdp-server` DEFAULT CHARACTER SET latin1;
create user 'pdp-serverrw'@'localhost' identified by 'secret';
grant all on `pdp-server`.* to 'pdp-serverrw'@'localhost';
```

# Getting started
This project uses Spring Boot and Maven. To run locally, type:

`mvn spring-boot:run`

When developing, it's convenient to just execute the applications main-method, which is in [PdpApplication](src/main/java/pdp/PdpApplication.java).

# Local database content

We don't provide flyway migrations to load initial policies. You need to work with the GUI to define and store them. However to test locally against
a database with policies you can load the same policies used in testing with the following command

`mysql -u root pdp-server < ./src/test/resources/sql/policies.sql`

# Testing

There are integration tests for PdpApplication that tests the various decisions against a running Spring app. The integration tests all use different configurations for loading
the policies. See [PdpApplicationDatabaseTest](src/test/java/pdp/PdpApplicationDatabaseTest.java) and [PdpApplicationTest](src/test/java/pdp/PdpApplicationTest.java)

One can also use cUrl to test against a running Spring application but you will need to load the policies as described in the previous step. 

Start the server and go the directory src/test/resources. Use the following command to test the permit decision:

`curl -i --user pdp_admin:secret -X POST --header "Content-Type: application/json" -d @./src/test/resources/SURFspotAccess.Permit.CategoriesShorthand.json http://localhost:8080/decide`

The directory src/test/resources contains additional test JSON inputs. To test against the test2 environment change the endpoint to `https://pdp.test2.surfconext.nl/decide`. 

Examples:

`curl -i --user pdp_admin:secret -X POST --header "Content-Type: application/json" -d @./src/test/resources/TeamAccess.Permit.json https://pdp.test2.surfconext.nl/decide`

`curl -i --user pdp_admin:secret -X POST --header "Content-Type: application/json" -d @./src/test/resources/SURFspotAccess.Deny.json https://pdp.test2.surfconext.nl/decide`

# Configuration and Deployment

On its classpath, the application has an [application.properties](src/main/resources/application.properties) file that
contains configuration defaults that are convenient when developing.

When the application actually gets deployed to a meaningful platform, it is pre-provisioned with ansible and the application.properties depends on
environment specific properties in the group_vars. See the project OpenConext-deploy and the role pdp for more information.

For details, see the [Spring Boot manual](http://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/).

