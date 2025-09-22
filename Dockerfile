FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon
RUN JAR_PATH=$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit) \
    && cp "$JAR_PATH" /workspace/app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
