# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY common /build/common
COPY lab-service /build/lab-service
RUN mvn -q -f /build/pom.xml -N install
RUN mvn -q -f /build/common/pom.xml install -DskipTests
RUN mvn -q -f /build/lab-service/pom.xml package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S hms && adduser -S hms -G hms
COPY --from=build /build/lab-service/target/lab-service.jar app.jar
RUN chown hms:hms app.jar
USER hms
EXPOSE 8086
HEALTHCHECK --interval=30s --timeout=5s --retries=5 --start-period=45s \
  CMD wget -qO- http://localhost:8086/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
