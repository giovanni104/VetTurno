package com.huellitas.vetturno.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errores = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.computeIfAbsent(error.getField(), key -> new ArrayList<>()).add(error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiError.crear(400, "Hay campos invalidos", errores));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> negocio(ReglaNegocioException ex) {
        return ResponseEntity.badRequest().body(ApiError.crear(400, ex.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class})
    public ResponseEntity<ApiError> entradaInvalida(Exception ex) {
        return ResponseEntity.badRequest().body(ApiError.crear(400, "Revise el JSON, los tipos de datos y los parametros"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest().body(ApiError.crear(400, "Los datos no cumplen las reglas de integridad"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> autenticacion(AuthenticationException ex) {
        return ResponseEntity.status(401).body(ApiError.crear(401, "Credenciales invalidas"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accesoDenegado(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(ApiError.crear(403, "No tiene permiso para realizar esta operacion"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex) {
        log.error("Fallo imprevisto al procesar una solicitud", ex);
        return ResponseEntity.internalServerError()
                .body(ApiError.crear(500, "Ocurrio un error interno. Intente nuevamente"));
    }
}
