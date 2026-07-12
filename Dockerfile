# Stage 1: Build stage
FROM maven:3.9.16-eclipse-temurin-25 AS build

# Set working directory
WORKDIR /build

# Copy only the pom.xml to cache dependencies
COPY pom.xml .

# Download dependencies (this will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy the API specification (needed for code generation) and source code
COPY api ./api
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:25-jre

# Metadata
LABEL maintainer="blade-runner-unamur"
LABEL description="Blade Runner Server Application"

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Create data directory for H2 database persistence (as per application.yml)
RUN mkdir -p /app/data && chown -R 1000:1000 /app/data

# Copy the jar from the build stage
COPY --from=build /build/target/spring-mem-1.0-SNAPSHOT.jar app.jar

# Expose port 8080 (as per application.yml and openapi.yaml)
EXPOSE 8080

ENV JAVA_OPTS="--enable-native-access=ALL-UNNAMED"
# Environment variables with defaults (as per application.yml)
ENV APP_URL=http://localhost:8080
# These should be provided at runtime
# ENV GITHUB_CLIENT_ID=
# ENV GITHUB_CLIENT_SECRET=
# ENV GITHUB_PAT=
# ENV BACKEND_TOKEN=
# ENV FRONTEND_TOKEN=
# ENV SONAR_TOKEN=

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
# Using non-root user for security (optional but recommended)
# USER 1000

ENTRYPOINT ["java", "-jar", "app.jar"]
