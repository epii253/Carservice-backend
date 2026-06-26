# ---------- Stage 1: Build ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle* gradle.properties* ./

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon || true

COPY src src

# Build & Test
RUN ./gradlew clean build --no-daemon

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "ru.svyatniy.Main"]
