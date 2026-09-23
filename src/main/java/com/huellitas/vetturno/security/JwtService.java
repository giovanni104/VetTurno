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

@Service
public class JwtService {
    private final SecretKey key;
    private final Duration expiration;
    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration}") Duration expiration, Clock clock) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
        this.clock = clock;
    }

    public String generar(String email) {
        return Jwts.builder()
                .issuer("vetturno")
                .subject(email)
                .issuedAt(Date.from(clock.instant()))
                .expiration(Date.from(clock.instant().plus(expiration)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String extraerEmail(String token) {
        Claims claims = Jwts.parser().verifyWith(key).requireIssuer("vetturno")
                .clock(() -> Date.from(clock.instant())).build()
                .parseSignedClaims(token).getPayload();
        if (claims.getSubject() == null || claims.getSubject().isBlank() || claims.getExpiration() == null) {
            throw new io.jsonwebtoken.MalformedJwtException("Token incompleto");
        }
        return claims.getSubject();
    }
}
