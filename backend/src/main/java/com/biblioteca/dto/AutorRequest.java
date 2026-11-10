package com.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AutorRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
        String nombre,

        @Size(max = 60, message = "La nacionalidad admite máximo 60 caracteres")
        String nacionalidad
) {
}
