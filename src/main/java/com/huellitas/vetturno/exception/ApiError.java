package com.huellitas.vetturno.exception;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public record ApiError(int status, String mensaje, Map<String, List<String>> errores, OffsetDateTime timestamp) {
    public static ApiError crear(int status, String mensaje, Map<String, List<String>> errores) {
        return new ApiError(status, mensaje, errores, OffsetDateTime.now(ZoneId.of("America/Bogota")));
    }

    public static ApiError crear(int status, String mensaje) {
        return crear(status, mensaje, Map.of());
    }
}
