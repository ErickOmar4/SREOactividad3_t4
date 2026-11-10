package com.biblioteca.service;

import com.biblioteca.dto.AutorRequest;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.model.Autor;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutorService {

    private final AutorRepository autores;
    private final LibroRepository libros;

    public AutorService(AutorRepository autores, LibroRepository libros) {
        this.autores = autores;
        this.libros = libros;
    }

    @Transactional(readOnly = true)
    public List<Autor> listar() {
        return autores.findAll();
    }

    @Transactional(readOnly = true)
    public Autor obtener(Long id) {
        return autores.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el autor con id " + id));
    }

    @Transactional
    public Autor crear(AutorRequest datos) {
        return autores.save(new Autor(datos.nombre().trim(), limpiar(datos.nacionalidad())));
    }

    @Transactional
    public Autor actualizar(Long id, AutorRequest datos) {
        Autor autor = obtener(id);
        autor.setNombre(datos.nombre().trim());
        autor.setNacionalidad(limpiar(datos.nacionalidad()));
        return autores.save(autor);
    }

    @Transactional
    public void eliminar(Long id) {
        Autor autor = obtener(id);
        if (libros.existsByAutorId(id)) {
            throw new ConflictoException("No se puede eliminar al autor porque tiene libros registrados");
        }
        autores.delete(autor);
    }

    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
