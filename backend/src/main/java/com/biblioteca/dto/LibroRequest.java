package com.biblioteca.dto;

import jakarta.validation.constraints.*;

/** Datos que el cliente envía para crear/actualizar un libro. La relación se expresa con autorId. */
public record LibroRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título admite máximo 200 caracteres")
        String titulo,

        @NotBlank(message = "El ISBN es obligatorio")
        @Size(max = 20, message = "El ISBN admite máximo 20 caracteres")
        String isbn,

        @Min(value = 1000, message = "El año debe ser mayor o igual a 1000")
        @Max(value = 2100, message = "El año debe ser menor o igual a 2100")
        Integer anioPublicacion,

        @Positive(message = "Las páginas deben ser un número positivo")
        Integer paginas,

        @NotNull(message = "Debes elegir un autor")
        Long autorId
) {
}
