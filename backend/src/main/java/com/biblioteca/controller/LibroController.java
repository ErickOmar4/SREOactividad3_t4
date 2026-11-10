package com.biblioteca.controller;

import com.biblioteca.dto.LibroRequest;
import com.biblioteca.dto.LibroResponse;
import com.biblioteca.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService servicio;

    public LibroController(LibroService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<LibroResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibroResponse crear(@Valid @RequestBody LibroRequest datos) {
        return servicio.crear(datos);
    }

    @PutMapping("/{id}")
    public LibroResponse actualizar(@PathVariable Long id, @Valid @RequestBody LibroRequest datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
