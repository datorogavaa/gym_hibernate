



FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS extractor

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

ENV JAVA_OPTS="-Xms256m -Xmx512m"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=extractor --chown=appuser:appgroup /app/dependencies/ ./
COPY --from=extractor --chown=appuser:appgroup /app/spring-boot-loader/ ./
COPY --from=extractor --chown=appuser:appgroup /app/snapshot-dependencies/ ./
COPY --from=extractor --chown=appuser:appgroup /app/application/ ./

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
