FROM gradle:8.7-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle clean shadowJar --no-daemon

FROM eclipse-temurin:21

WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar
COPY --from=build /app/src/main/resources ./src/main/resources

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]