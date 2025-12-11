FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/app.jar app.jar

COPY application.yml application.yml

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.yml"]