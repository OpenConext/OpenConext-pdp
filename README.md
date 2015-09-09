# OpenConext-pdp
OpenConext implementation of a XACML based PDP engine for access policy enforcement

# Getting started
This project uses Spring Boot and Maven. To run locally, type:

`mvn spring-boot:run`

When developing, it's convenient to just execute the applications main-method, which is in [PdpApplication](src/main/java/pdp/PdpApplication).

# Testing

There is an integration test for PdpApplication that tests the various decisions against a running Spring app. 

One can also use cUrl to test. Start the server and go the directory src/test/resources. Use the following commands to test the different decisions:

`curl -i --user admin:secret -X POST --header "Content-Type: application/json" -d @Request.01.Deny.json http://localhost:8080/decide`

`curl -i --user admin:secret -X POST --header "Content-Type: application/json" -d @Request.01.NA.json http://localhost:8080/decide`

`curl -i --user admin:secret -X POST --header "Content-Type: application/json" -d @Request.01.Permit.CategoriesShorthand.json http://localhost:8080/decide`

`curl -i --user admin:secret -X POST --header "Content-Type: application/json" -d @Request.01.Permit.json http://localhost:8080/decide`

# Configuration and Deployment

On its classpath, the application has an [application.properties](src/main/resources/application.properties) file that
contains configuration defaults that are convenient when developing.

When the application actually gets deployed to a meaningful platform, it is pre-provisioned with ansible and the application.properties depends on
environment specific properties in the group_vars. See the project OpenConext-deploy and the role pdp for more information.

For details, see the [Spring Boot manual](http://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/).

