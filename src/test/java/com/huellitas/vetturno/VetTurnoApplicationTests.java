package com.huellitas.vetturno;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

// Opt-in: crea solo registros de prueba propios y no elimina datos existentes.
// No sustituye las evidencias manuales de Swagger, Workbench y reinicio.
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "VETTURNO_MYSQL_TEST", matches = "true")
class VetTurnoApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired PasswordEncoder encoder;
    private final List<Map<String, Object>> evidence = new ArrayList<>();

    private JsonNode postJson(String path, Object body, String token, int expected) throws Exception {
        var request = post(path).contentType("application/json").content(mapper.writeValueAsString(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        MvcResult result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getStatus()).as(path).isEqualTo(expected);
        JsonNode json = mapper.readTree(result.getResponse().getContentAsString());
        evidence.add(Map.of("ruta", path, "estado", expected,
                "respuesta", path.startsWith("/api/auth/") && expected == 200 ? Map.of("token", "[OCULTO]") : json));
        return json;
    }

    private JsonNode getJson(String path, String token, int expected) throws Exception {
        var request = get(path);
        if (token != null) request.header("Authorization", "Bearer " + token);
        MvcResult result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getStatus()).as(path).isEqualTo(expected);
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void flujoCompletoConMySqlRealYReservaConcurrente() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
        }
        String suffix = UUID.randomUUID().toString();
        String userEmail = "paula-" + suffix + "@example.com";
        String adminEmail = "marta-" + suffix + "@example.com";
        String password = UUID.randomUUID() + "Ab1";
        String user = postJson("/api/auth/register",
                Map.of("email", userEmail, "password", password, "rol", "ADMIN"), null, 200).get("token").asText();
        assertThat(jdbc.queryForObject("select rol from usuarios where email = ?", String.class, userEmail)).isEqualTo("USER");
        String hash = jdbc.queryForObject("select password from usuarios where email = ?", String.class, userEmail);
        assertThat(hash).startsWith("$2").isNotEqualTo(password);
        assertThat(encoder.matches(password, hash)).isTrue();
        JsonNode invalid = postJson("/api/auth/register", Map.of("email", "mal", "password", "123"), null, 400);
        assertThat(invalid.path("errores").has("email")).isTrue();
        assertThat(invalid.path("errores").has("password")).isTrue();
        user = postJson("/api/auth/login", Map.of("email", userEmail, "password", password), null, 200).get("token").asText();
        getJson("/api/citas", null, 401);
        postJson("/api/veterinarios", Map.of("nombre", "Andres " + suffix, "especialidad", "General"), user, 403);

        postJson("/api/auth/register", Map.of("email", adminEmail, "password", password), null, 200);
        // Equivale a la habilitacion controlada en la base exigida por el taller.
        assertThat(jdbc.update("update usuarios set rol = 'ADMIN' where email = ?", adminEmail)).isEqualTo(1);
        String admin = postJson("/api/auth/login", Map.of("email", adminEmail, "password", password), null, 200)
                .get("token").asText();
        long vet = postJson("/api/veterinarios", Map.of("nombre", "Andres " + suffix, "especialidad", "General"),
                admin, 201).get("id").asLong();
        long owner = postJson("/api/propietarios", Map.of("nombre", "Responsable " + suffix, "telefono", "3001234567"),
                user, 201).get("id").asLong();
        JsonNode pet = postJson("/api/mascotas", Map.of("nombre", "Luna", "especie", "Canino", "propietarioId", owner),
                user, 201);
        assertThat(pet.get("propietarioId").asLong()).isEqualTo(owner);
        assertThat(pet.has("citas")).isFalse();
        long petId = pet.get("id").asLong();
        postJson("/api/mascotas", Map.of("nombre", "Sin responsable", "especie", "Canino", "propietarioId", Long.MAX_VALUE),
                user, 400);
        LocalDateTime future = LocalDateTime.now(ZoneId.of("America/Bogota")).plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Map<String, Object> cita = Map.of("fechaHora", future.toString(), "motivo", "Control", "mascotaId", petId, "veterinarioId", vet);
        long citaId = postJson("/api/citas", cita, user, 201).get("id").asLong();
        postJson("/api/citas", Map.of("fechaHora", "2000-01-01T09:00:00", "motivo", "Pasada",
                "mascotaId", petId, "veterinarioId", vet), user, 400);
        postJson("/api/citas", cita, user, 400);
        postJson("/api/citas", Map.of("fechaHora", future.minusHours(1).toString(), "motivo", "Primera",
                "mascotaId", petId, "veterinarioId", vet), user, 201);
        JsonNode agenda = getJson("/api/citas/veterinario/" + vet, user, 200);
        assertThat(agenda.size()).isEqualTo(2);
        assertThat(agenda.get(0).get("motivo").asText()).isEqualTo("Primera");
        assertThat(agenda.get(1).get("id").asLong()).isEqualTo(citaId);

        String requestBody = mapper.writeValueAsString(Map.of("fechaHora", future.plusHours(1).toString(),
                "motivo", "Concurrente", "mascotaId", petId, "veterinarioId", vet));
        final String bearer = user;
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> reserve = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Tiempo de espera agotado");
            return mvc.perform(post("/api/citas").header("Authorization", "Bearer " + bearer)
                    .contentType("application/json").content(requestBody)).andReturn().getResponse().getStatus();
        };
        try {
            Future<Integer> first = pool.submit(reserve);
            Future<Integer> second = pool.submit(reserve);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 400);
        } finally {
            pool.shutdownNow();
        }
        assertThat(jdbc.queryForObject("select count(*) from citas where veterinario_id = ? and fecha_hora = ?",
                Integer.class, vet, future.plusHours(1))).isEqualTo(1);
        JsonNode openapi = getJson("/v3/api-docs", null, 200);
        assertThat(openapi.path("components").path("securitySchemes").has("bearerAuth")).isTrue();
        assertThat(openapi.path("paths").size()).isEqualTo(7);
        assertThat(mvc.perform(get("/swagger-ui/index.html")).andReturn().getResponse().getStatus()).isEqualTo(200);

        Files.createDirectories(Path.of("target", "evidencias"));
        mapper.writerWithDefaultPrettyPrinter().writeValue(Path.of("target", "evidencias", "mysql-integracion.json").toFile(),
                Map.of("tipo", "Prueba automatizada con MySQL real; no es prueba manual",
                        "resultados", evidence, "concurrencia", List.of(201, 400), "citaPersistidaId", citaId));
    }
}
