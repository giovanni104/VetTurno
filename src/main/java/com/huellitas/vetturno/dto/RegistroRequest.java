package com.huellitas.vetturno.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistroRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser valido") String email,
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres") String password) {
}
