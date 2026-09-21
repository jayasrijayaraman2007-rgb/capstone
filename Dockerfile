# Production image for Project 126 — Sovereign Gate.
# Build:  docker build -t visitorgate .
# Run:    docker run -p 8080:8080 --env-file .env.prod visitorgate
# No secrets are baked in: configure DB_*, MAIL_*, ADMIN_* via platform env vars.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd --create-home --uid 10001 appuser
USER appuser
WORKDIR /app
COPY --from=build /app/target/visitor-gate-pass-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
