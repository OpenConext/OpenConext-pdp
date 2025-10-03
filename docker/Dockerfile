FROM eclipse-temurin:21-jdk-alpine
COPY target/*.jar /app.jar
COPY ./target/classes/xacml/policies/  /files/xacml/policies/
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
