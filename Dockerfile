# Multi-stage build for Spring Boot application
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build application
RUN gradle build -x test --no-daemon

# Runtime stage
FROM openjdk:21-jdk-slim

WORKDIR /app

# Create non-root user
RUN groupadd -g 1000 spring && useradd -u 1000 -g spring -s /bin/bash spring

# Create necessary directories
RUN mkdir -p /var/log/sharegym && \
    chown -R spring:spring /var/log/sharegym

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", "app.jar"]