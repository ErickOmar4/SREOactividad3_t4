package com.biblioteca.model;

import jakarta.persistence.*;

/** Lado "muchos" de la relación: muchos Libros pertenecen a un Autor (llave foránea autor_id). */
@Entity
@Table(name = "libro")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    private Integer paginas;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Autor autor;

    public Libro() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(Integer anioPublicacion) { this.anioPublicacion = anioPublicacion; }

    public Integer getPaginas() { return paginas; }
    public void setPaginas(Integer paginas) { this.paginas = paginas; }

    public Autor getAutor() { return autor; }
    public void setAutor(Autor autor) { this.autor = autor; }
}
