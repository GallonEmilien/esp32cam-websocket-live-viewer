FROM gradle:jdk21 AS builder
WORKDIR /app
COPY backend /app
RUN gradle clean build --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]