FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY pom.xml /app/pom.xml
COPY src /app/src
WORKDIR /app
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
