# syntax=docker/dockerfile:1
# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY common /build/common
COPY config-server /build/config-server
RUN --mount=type=cache,target=/root/.m2/repository mvn -q -f /build/pom.xml -N install
RUN --mount=type=cache,target=/root/.m2/repository mvn -q -f /build/common/pom.xml install -DskipTests
RUN --mount=type=cache,target=/root/.m2/repository mvn -q -f /build/config-server/pom.xml package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd -r hms && useradd -r -g hms hms
COPY --from=build /build/config-server/target/config-server.jar app.jar
RUN chown hms:hms app.jar
USER hms
EXPOSE 8888
HEALTHCHECK --interval=30s --timeout=5s --retries=5 --start-period=30s \
  CMD wget -qO- http://localhost:8888/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
