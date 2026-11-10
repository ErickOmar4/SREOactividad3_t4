package com.biblioteca.service;

import com.biblioteca.dto.LibroRequest;
import com.biblioteca.dto.LibroResponse;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Libro;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Lógica de negocio de Libro: validaciones de reglas y manejo de la relación con Autor. */
@Service
public class LibroService {

    private final LibroRepository libros;
    private final AutorRepository autores;

    public LibroService(LibroRepository libros, AutorRepository autores) {
        this.libros = libros;
        this.autores = autores;
    }

    @Transactional(readOnly = true)
    public List<LibroResponse> listar() {
        return libros.findAllConAutor().stream().map(LibroResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return LibroResponse.desde(buscar(id));
    }

    @Transactional
    public LibroResponse crear(LibroRequest datos) {
        String isbn = datos.isbn().trim();
        if (libros.existsByIsbn(isbn)) {
            throw new ConflictoException("Ya existe un libro con el ISBN " + isbn);
        }
        Libro libro = new Libro();
        copiar(datos, libro);
        return LibroResponse.desde(libros.save(libro));
    }

    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest datos) {
        Libro libro = buscar(id);
        String isbn = datos.isbn().trim();
        if (libros.existsByIsbnAndIdNot(isbn, id)) {
            throw new ConflictoException("Ya existe otro libro con el ISBN " + isbn);
        }
        copiar(datos, libro);
        return LibroResponse.desde(libros.save(libro));
    }

    @Transactional
    public void eliminar(Long id) {
        libros.delete(buscar(id));
    }

    private Libro buscar(Long id) {
        return libros.findByIdConAutor(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro con id " + id));
    }

    private void copiar(LibroRequest datos, Libro libro) {
        Autor autor = autores.findById(datos.autorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el autor con id " + datos.autorId()));
        libro.setTitulo(datos.titulo().trim());
        libro.setIsbn(datos.isbn().trim());
        libro.setAnioPublicacion(datos.anioPublicacion());
        libro.setPaginas(datos.paginas());
        libro.setAutor(autor);
    }
}
