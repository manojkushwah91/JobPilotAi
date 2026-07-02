# ============================================================
# Stage 1: Build
# ============================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache dependencies
COPY backend/pom.xml .
COPY backend/jobpilot-common/pom.xml backend/jobpilot-common/
COPY backend/jobpilot-domain/pom.xml backend/jobpilot-domain/
COPY backend/jobpilot-application/pom.xml backend/jobpilot-application/
COPY backend/jobpilot-infrastructure/pom.xml backend/jobpilot-infrastructure/
COPY backend/jobpilot-interfaces/pom.xml backend/jobpilot-interfaces/
COPY backend/jobpilot-bootstrap/pom.xml backend/jobpilot-bootstrap/

RUN mvn dependency:go-offline -B -f pom.xml

# Build
COPY backend/ .
RUN mvn package -DskipTests -B -f pom.xml -Pproduction

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S jobpilot && adduser -S jobpilot -G jobpilot

USER jobpilot
WORKDIR /app

COPY --from=builder /build/jobpilot-bootstrap/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:MaxGCPauseMillis=100", \
    "-XX:SoftMaxHeapSize=4g", \
    "-Xms2g", \
    "-Xmx4g", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
