# First stage: Build the application
FROM openjdk:21 AS build

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory in the build stage
WORKDIR /app

# Copy the Maven project files to the build stage
COPY pom.xml .
COPY src ./src

# Clean and package the application
RUN mvn clean package -DskipTests

# Second stage: Build the final image
FROM openjdk:21

# Set the working directory in the container
WORKDIR /app

# Copy the packaged JAR file from the build stage to the final image
COPY --from=build /app/target/HNG-Security-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8086

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
