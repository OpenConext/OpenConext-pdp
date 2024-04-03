FROM eclipse-temurin:11-jdk-alpine
COPY target/*.jar /app.jar
COPY ./target/classes/xacml/policies/  /files/xacml/policies/
ENTRYPOINT ["java","-jar","/app.jar"]
