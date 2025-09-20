# syntax=docker/dockerfile:1

############################################
# Build stage
############################################
FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /home/gradle/project

# Pre-fetch wrapper and dependency metadata for better caching
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle gradlew ./gradlew
COPY --chown=gradle:gradle gradlew.bat ./gradlew.bat
COPY --chown=gradle:gradle settings.gradle build.gradle ./

# Copy source last to maximize Docker layer caching
COPY --chown=gradle:gradle src ./src

RUN chmod +x gradlew \
    && ./gradlew --no-daemon bootJar

############################################
# Runtime stage
############################################
FROM eclipse-temurin:21-jre-alpine AS runtime

ENV SPRING_PROFILES_ACTIVE=prod

WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/*SNAPSHOT.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
