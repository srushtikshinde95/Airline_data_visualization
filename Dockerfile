# ==============================================================
# Stage 1: Build stage with Maven and Eclipse Temurin Java 17
# ==============================================================
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory for the build stage
WORKDIR /build

# Copy Maven POM and project source files
COPY pom.xml .
COPY src ./src

# Build the project using Maven (creates shaded executable JAR)
RUN mvn clean package -DskipTests

# ==============================================================
# Stage 2: Runtime stage with Eclipse Temurin Java 17 JRE
# ==============================================================
FROM eclipse-temurin:17-jre

# Set application working directory to /app
WORKDIR /app

# Configure default port for Render (Render sets PORT dynamically; fallback to 10000)
ENV PORT=10000

# Copy the built jar from target/ into the runtime image as app.jar
COPY --from=build /build/target/airline-data-visualization.jar app.jar

# Copy the required data/ directory into the container, preserving its relative path
COPY data ./data

# Expose port 10000
EXPOSE 10000

# Start the application
CMD ["java", "-jar", "app.jar"]
