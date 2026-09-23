package com.huellitas.vetturno.dto;

import jakarta.validation.constraints.*;

public record MascotaRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "La especie es obligatoria") String especie,
        String raza,
        @NotNull(message = "El propietario es obligatorio")
        @Positive(message = "El propietarioId debe ser positivo") Long propietarioId) {
}
