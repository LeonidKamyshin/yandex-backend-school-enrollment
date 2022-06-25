# syntax=docker/dockerfile:1
FROM openjdk:17 as base

WORKDIR .

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

COPY src ./src

EXPOSE 80
CMD ["./mvnw", "spring-boot:run"]