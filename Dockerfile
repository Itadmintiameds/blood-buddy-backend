# Use Amazon Corretto Java 21
FROM amazoncorretto:21

# Set the working directory inside the container
WORKDIR /blood-buddy

# Copy the Spring Boot executable JAR
COPY target/backend-0.0.1-SNAPSHOT.jar blood-buddy.jar

# Expose Spring Boot application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "blood-buddy.jar"]