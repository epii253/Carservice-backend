# ---------- Stage 1: Build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY settings.gradle /app/settings.gradle
COPY build.gradle /app/build.gradle

COPY common/ /app/common/
COPY storageService/ /app/storageService/

RUN chmod +x gradlew

RUN ./gradlew :storageService:bootJar --no-daemon

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/storageService/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]