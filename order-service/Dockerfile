FROM gradle:8.6-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :order-service:bootJar -x test

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/order-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
