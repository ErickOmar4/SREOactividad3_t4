package com.biblioteca.dto;

import com.biblioteca.model.Libro;

/** Respuesta al cliente: incluye el nombre del autor (no solo su ID). */
public record LibroResponse(
        Long id,
        String titulo,
        String isbn,
        Integer anioPublicacion,
        Integer paginas,
        Long autorId,
        String autorNombre
) {
    public static LibroResponse desde(Libro l) {
        return new LibroResponse(
                l.getId(),
                l.getTitulo(),
                l.getIsbn(),
                l.getAnioPublicacion(),
                l.getPaginas(),
                l.getAutor().getId(),
                l.getAutor().getNombre()
        );
    }
}
