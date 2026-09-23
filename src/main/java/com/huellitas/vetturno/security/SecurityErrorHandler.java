package com.huellitas.vetturno.security;

import com.huellitas.vetturno.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Devuelve errores de seguridad en formato JSON usando ApiError.
 * 401 significa que falta una autenticacion valida; 403 significa que falta permiso.
 * Se necesita esta clase porque los filtros trabajan antes del controlador,
 * donde actua el manejador global de excepciones.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Se ejecuta cuando falta el token o no es valido, por ejemplo si vencio.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        escribir(response, 401, "Se requiere un token valido y vigente");
    }

    /**
     * Se ejecuta cuando el usuario esta autenticado, pero su rol no permite la accion.
     * Por ejemplo, cuando USER intenta registrar un veterinario.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        escribir(response, 403, "No tiene permiso para realizar esta operacion");
    }

    /**
     * Escribe el codigo HTTP y el mismo formato de error que usa el resto de la API.
     * ObjectMapper convierte el objeto ApiError en texto JSON.
     */
    private void escribir(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiError.crear(status, mensaje)));
    }
}
