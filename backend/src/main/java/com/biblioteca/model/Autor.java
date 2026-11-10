package com.biblioteca.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Lado "uno" de la relación: un Autor tiene muchos Libros. */
@Entity
@Table(name = "autor")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 60)
    private String nacionalidad;

    // mappedBy: la llave foránea vive en Libro (campo "autor"), aquí solo se espeja la relación
    @OneToMany(mappedBy = "autor")
    private List<Libro> libros = new ArrayList<>();

    public Autor() {
    }

    public Autor(String nombre, String nacionalidad) {
        this.nombre = nombre;
        this.nacionalidad = nacionalidad;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    public List<Libro> getLibros() { return libros; }
    public void setLibros(List<Libro> libros) { this.libros = libros; }
}
