# Compilar y probar el proyecto sin instalar Java ni Maven en el equipo.
FROM maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package

# Ejecutar unicamente el JAR con Java 17.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/vetturno-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
