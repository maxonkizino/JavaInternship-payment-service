FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY src src

RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S paymentservice && adduser -S paymentservice -G paymentservice

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN chown -R paymentservice:paymentservice /app

USER paymentservice

EXPOSE 8084

ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8084
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
