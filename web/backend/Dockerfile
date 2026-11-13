FROM eclipse-temurin:25-jdk AS build

WORKDIR /build

COPY . .

RUN ./gradlew :web:backend:build --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /build/web/backend/build/libs/*.jar mycelium-web-backend.jar

ENTRYPOINT ["java", "-jar", "mycelium-web-backend.jar"]
