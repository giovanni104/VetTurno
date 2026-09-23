package com.huellitas.vetturno.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser valido") String email,
        @NotBlank(message = "La contrasena es obligatoria") String password) {
}
