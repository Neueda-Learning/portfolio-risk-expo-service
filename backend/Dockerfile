# Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy the entire backend/app project
COPY backend/app/ .

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S app && \
    adduser -S app -u 1001

# Copy compiled jar from builder
COPY --from=builder --chown=app:app /app/target/*.jar app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
