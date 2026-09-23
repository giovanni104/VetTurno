package com.huellitas.vetturno.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CitaRequest(
        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La cita debe tener una fecha futura")
        @Schema(description = "Hora de Colombia (America/Bogota), sin offset y con segundos en cero",
                example = "2030-10-20T09:00:00")
        LocalDateTime fechaHora,
        @NotBlank(message = "El motivo es obligatorio") String motivo,
        @NotNull(message = "La mascota es obligatoria")
        @Positive(message = "El mascotaId debe ser positivo") Long mascotaId,
        @NotNull(message = "El veterinario es obligatorio")
        @Positive(message = "El veterinarioId debe ser positivo") Long veterinarioId) {
}
