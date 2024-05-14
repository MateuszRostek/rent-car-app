FROM openjdk:17-jdk-slim
WORKDIR application
COPY target/application.jar ./
ENTRYPOINT ["java", "-jar","application.jar"]