FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY jobpilot-common/pom.xml jobpilot-common/pom.xml
COPY jobpilot-domain/pom.xml jobpilot-domain/pom.xml
COPY jobpilot-application/pom.xml jobpilot-application/pom.xml
COPY jobpilot-infrastructure/pom.xml jobpilot-infrastructure/pom.xml
COPY jobpilot-interfaces/pom.xml jobpilot-interfaces/pom.xml
COPY jobpilot-bootstrap/pom.xml jobpilot-bootstrap/pom.xml
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /app/jobpilot-bootstrap/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
