FROM ghcr.io/openconext/openconext-basecontainers/java21:latest
COPY target/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
