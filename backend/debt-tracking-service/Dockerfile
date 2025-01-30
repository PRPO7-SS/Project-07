# Uporabi uradno OpenJDK sliko
FROM openjdk:17-jdk-slim

# Namesti curl za healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Nastavi delovno mapo
WORKDIR /app

# Kopiraj re-packaged JAR iz builda
COPY target/debt-tracking-service-1.0-SNAPSHOT.jar app.jar

# Expose porta za dostop
EXPOSE 8086

# Zaženi mikrostoritev
ENTRYPOINT ["java", "-jar", "app.jar"]