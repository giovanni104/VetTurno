package com.huellitas.vetturno.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Crea y revisa los JWT que usamos para identificar a los usuarios.
 * Un JWT es un texto firmado: permite detectar cambios, pero no oculta su contenido.
 * Por eso guardamos el email y las fechas, nunca la contrasena.
 */
@Service
public class JwtService {
    private final SecretKey key;
    private final Duration expiration;
    private final Clock clock;

    /**
     * Recibe la clave y la duracion desde la configuracion.
     * La duracion del proyecto es una hora. Clock nos da la hora actual
     * y permite usar una hora fija cuando hacemos pruebas.
     */
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration}") Duration expiration, Clock clock) {
        // La clave llega escrita en Base64; la convertimos al formato que usa JJWT.
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
        this.clock = clock;
    }

    /**
     * Devuelve un token firmado para el usuario que se registro o inicio sesion.
     * issuer indica quien lo emite; subject contiene el email del usuario.
     */
    public String generar(String email) {
        return Jwts.builder()
                .issuer("vetturno")
                .subject(email)
                .issuedAt(Date.from(clock.instant()))
                .expiration(Date.from(clock.instant().plus(expiration)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Comprueba la firma, el emisor y el vencimiento antes de devolver el email.
     * Si el token fue alterado, esta vencido o esta incompleto, lanza una excepcion.
     * JwtAuthFilter se encarga de convertir ese problema en una respuesta 401.
     */
    public String extraerEmail(String token) {
        Claims claims = Jwts.parser().verifyWith(key).requireIssuer("vetturno")
                .clock(() -> Date.from(clock.instant())).build()
                .parseSignedClaims(token).getPayload();
        // Tambien exigimos que el token tenga email y fecha de vencimiento.
        if (claims.getSubject() == null || claims.getSubject().isBlank() || claims.getExpiration() == null) {
            throw new io.jsonwebtoken.MalformedJwtException("Token incompleto");
        }
        return claims.getSubject();
    }
}
