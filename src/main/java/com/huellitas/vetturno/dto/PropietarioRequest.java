package com.huellitas.vetturno.dto;

import jakarta.validation.constraints.*;

public record PropietarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "El telefono es obligatorio")
        @Pattern(regexp = "\\+?[0-9]{7,15}", message = "El telefono debe tener entre 7 y 15 digitos y un + inicial opcional")
        String telefono,
        @Email(message = "El email debe ser valido")
        String email) {
}
