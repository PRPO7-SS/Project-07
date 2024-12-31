# Use the official OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the re-packaged JAR from the build output
COPY target/savings-goal-service-1.0-SNAPSHOT.jar app.jar



# Expose the service port (adjust if needed)
EXPOSE 8084

# Run the service
ENTRYPOINT ["java", "-jar", "app.jar"]