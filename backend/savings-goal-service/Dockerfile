# Uporabi uradni OpenJDK 17 slim image
FROM openjdk:17-jdk-slim

# Nastavi delovno mapo
WORKDIR /app

# Kopiraj prepakiran JAR iz build output-a
COPY target/savings-goal-service-1.0-SNAPSHOT.jar app.jar

# Namesti curl za healthcheck (uporablja se v docker-compose)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Expose port za storitev
EXPOSE 8084

# Zaženi aplikacijo
ENTRYPOINT ["java", "-jar", "app.jar"]