# VetTurno

Agenda digital de Veterinaria Huellitas. Taller universitario del modulo 3 de Java AI Engineer.

Paula registra responsables y mascotas y agenda citas para el doctor Andres. Dona Marta, como ADMIN, tambien registra veterinarios. La API sustituye la informacion dispersa en el cuaderno y WhatsApp por datos persistentes y relacionados.

## Tecnologias

Java 17, Spring Boot 4.1.0, Maven Wrapper, MySQL, JPA/Hibernate, Spring Security, JJWT 0.13.0 y springdoc/OpenAPI 3.1.0.

## Como ejecutar

Requiere JDK 17 y una base MySQL llamada `vetturno`. Configura `DB_USER`, `DB_PASSWORD` y `JWT_SECRET` de forma local, sin agregarlos a Git. Ejecuta `./mvnw spring-boot:run` o `.\mvnw.cmd spring-boot:run` en Windows.

Base descargada de Spring Initializr. Implementacion y evidencias en preparacion: este archivo se completara con el contrato, las pruebas reales y la guia de entrega.
