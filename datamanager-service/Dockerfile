# Chemello Francesco
# Dockerfile for Datamanager Service

# Use Java 21 JRE as the base image

FROM eclipse-temurin:21-jre 
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]