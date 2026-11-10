package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    boolean existsByAutorId(Long autorId);

    /** JOIN FETCH evita el problema N+1 al mostrar el nombre del autor. */
    @Query("select l from Libro l join fetch l.autor order by l.id")
    List<Libro> findAllConAutor();

    @Query("select l from Libro l join fetch l.autor where l.id = :id")
    java.util.Optional<Libro> findByIdConAutor(Long id);
}
