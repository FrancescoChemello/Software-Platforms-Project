# Chemello Francesco
# Dockerfile for MongoDB Service

# Use Java 21 JRE as the base image

FROM eclipse-temurin:21-jre 
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]