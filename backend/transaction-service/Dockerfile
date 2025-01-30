# Use the official OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the re-packaged JAR from the build output
COPY target/transaction-service-1.0-SNAPSHOT.jar app.jar

# Expose the service port (adjust if needed)
EXPOSE 8081

# Run the service
ENTRYPOINT ["java", "-jar", "app.jar"]