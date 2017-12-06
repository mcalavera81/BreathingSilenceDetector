FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
COPY .  /app
RUN mvn install

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/acurable.jar /app
ENTRYPOINT ["java", "-jar", "acurable.jar"]