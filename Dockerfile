# ---- Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package
RUN echo "=== TARGET CONTENTS ===" && ls -lah /app/target

# ---- Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/app.jar /app/app.jar
ENV PORT=8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
