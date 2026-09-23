package com.huellitas.vetturno.security;

import io.jsonwebtoken.JwtException;
import java.time.*;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    // Material exclusivo de pruebas, nunca usado por la configuracion de la aplicacion.
    private final String secret = Base64.getEncoder().encodeToString(new byte[32]);
    private final Instant now = Instant.parse("2030-01-01T15:00:00Z");

    private JwtService service(Instant instant) {
        return new JwtService(secret, Duration.ofHours(1), Clock.fixed(instant, ZoneId.of("America/Bogota")));
    }

    @Test
    void tokenVigenteContieneIdentidad() {
        String token = service(now).generar("paula@example.com");
        assertThat(service(now.plusSeconds(3599)).extraerEmail(token)).isEqualTo("paula@example.com");
    }

    @Test
    void tokenExpiraDespuesDeUnaHora() {
        String token = service(now).generar("paula@example.com");
        assertThatThrownBy(() -> service(now.plusSeconds(3601)).extraerEmail(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaTokenManipulado() {
        String token = service(now).generar("paula@example.com");
        String[] partes = token.split("\\.");
        assertThatThrownBy(() -> service(now).extraerEmail(partes[0] + "." + partes[1] + ".firma-falsa"))
                .isInstanceOf(JwtException.class);
    }
}
