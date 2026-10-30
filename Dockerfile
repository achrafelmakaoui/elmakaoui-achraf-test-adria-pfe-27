# One Dockerfile for ALL micro-services. The service is chosen with --build-arg MODULE=<module-name>
#   docker build --build-arg MODULE=account-service -t adria/account-service .

# ---- Stage 1: build the jar with Maven (JDK 21)
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /workspace
COPY pom.xml .
COPY discovery-service/pom.xml discovery-service/pom.xml
COPY config-service/pom.xml config-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY transaction-service/pom.xml transaction-service/pom.xml
COPY transaction-consumer/pom.xml transaction-consumer/pom.xml
COPY chatbot-service/pom.xml chatbot-service/pom.xml
# download dependencies first: this layer is cached while the poms do not change
RUN mvn -B -q -pl ${MODULE} -am dependency:go-offline || true
COPY . .
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests

# ---- Stage 2: small runtime image (JRE only, non-root user)
FROM eclipse-temurin:21-jre
ARG MODULE
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1001 appuser
WORKDIR /app
COPY --from=build /workspace/${MODULE}/target/app.jar app.jar
USER appuser
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
