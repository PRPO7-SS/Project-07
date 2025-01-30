# Use the official OpenJDK image
FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the re-packaged JAR from the build output
COPY target/budget-service-1.0-SNAPSHOT.jar app.jar

# Expose the service port (adjust if needed)
EXPOSE 8083

# Run the service
ENTRYPOINT ["java", "-jar", "app.jar"]