package com.huellitas.vetturno.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Revisa el token que llega en el encabezado Authorization de una solicitud.
 * Si el token es valido, informa a Spring Security quien es el usuario.
 * Los permisos de ese usuario se revisan despues, segun SecurityConfig.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioDetailsService usuarios;
    private final SecurityErrorHandler errors;

    public JwtAuthFilter(JwtService jwt, UsuarioDetailsService usuarios, SecurityErrorHandler errors) {
        this.jwt = jwt;
        this.usuarios = usuarios;
        this.errors = errors;
    }

    /**
     * Registro, login y Swagger se pueden usar sin token.
     * Por eso no hacemos esta revision en esas rutas.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/register") || path.equals("/api/auth/login")
                || path.equals("/swagger-ui.html") || path.startsWith("/swagger-ui/")
                || path.equals("/v3/api-docs") || path.startsWith("/v3/api-docs/");
    }

    /**
     * Lee el encabezado con el formato Bearer seguido del token.
     * Un token incorrecto produce 401 y detiene esta solicitud.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.regionMatches(true, 0, "Bearer ", 0, 7) || header.substring(7).isBlank()) {
                    throw new BadCredentialsException("Token invalido");
                }
                // JwtService valida el token antes de devolver el email.
                // Luego buscamos el usuario y su rol actual en la base de datos.
                var user = usuarios.loadUserByUsername(jwt.extraerEmail(header.substring(7).strip()));
                // No volvemos a pedir la contrasena: el token ya fue validado.
                var authentication = UsernamePasswordAuthenticationToken.authenticated(
                        user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Guardamos la identidad para que Spring revise los permisos
                // durante esta solicitud. No creamos una sesion de usuario.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
                // Si algo falla, quitamos la identidad y devolvemos el error.
                SecurityContextHolder.clearContext();
                errors.commence(request, response, new BadCredentialsException("Token invalido", ex));
                return;
            }
        }
        // Continuamos con los siguientes filtros.
        // Si no llego un token, Spring Security decidira si la ruta permite entrar.
        chain.doFilter(request, response);
    }
}
