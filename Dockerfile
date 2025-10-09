# syntax=docker/dockerfile:1

# ============================================
# Stage 1: Build stage
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached layer if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build the application (skip tests - already run in GitHub Actions)
RUN ./mvnw package -DskipTests -B && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# ============================================
# Stage 2: Runtime stage
# ============================================
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

USER spring:spring

WORKDIR /app

# Copy only the necessary artifacts from build stage
ARG DEPENDENCY=/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "gruppe9ek.kino.KinoApplication"]
