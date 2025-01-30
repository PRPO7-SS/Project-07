# Use the official OpenJDK 17 slim image
FROM openjdk:17-jdk-slim

# Install curl for health checks (needed for container healthcheck in docker-compose)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the JAR file from the build output
COPY target/investment-service-1.0-SNAPSHOT.jar app.jar

# Expose the service port (adjust if needed)
EXPOSE 8085

# Define health check for Docker
HEALTHCHECK --interval=10s --timeout=5s --retries=3 CMD curl -f http://localhost:8085/investments/health || exit 1

# Run the service
ENTRYPOINT ["java", "-jar", "app.jar"]