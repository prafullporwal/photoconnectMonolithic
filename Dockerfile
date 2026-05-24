# =============================================================================
# Single-stage Docker build for the PhotoConnect monolith
# =============================================================================
# REQUIRES (pre-built on host before `docker compose --profile app up --build`):
#   1. target/*.jar    →  mvn clean package -DskipTests
#   2. keys/*.pem      →  ./scripts/generate-keys.{sh,ps1}
#
# Why host-built JARs? `mvn` inside the image can't reach Maven Central from
# this network — same workaround the sibling microservices repo uses.
# =============================================================================
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
USER app
WORKDIR /app
COPY --chown=app:app target/*.jar /app/app.jar
COPY --chown=app:app keys/ /app/keys/
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
