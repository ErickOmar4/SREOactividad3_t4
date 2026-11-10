package com.biblioteca.controller;

import com.biblioteca.dto.AutorRequest;
import com.biblioteca.model.Autor;
import com.biblioteca.service.AutorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorService servicio;

    public AutorController(AutorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return servicio.listar().stream().map(AutorController::aMapa).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> obtener(@PathVariable Long id) {
        return aMapa(servicio.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crear(@Valid @RequestBody AutorRequest datos) {
        return aMapa(servicio.crear(datos));
    }

    @PutMapping("/{id}")
    public Map<String, Object> actualizar(@PathVariable Long id, @Valid @RequestBody AutorRequest datos) {
        return aMapa(servicio.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    /** Se devuelve un mapa simple para no exponer la lista perezosa de libros (evita ciclos JSON). */
    private static Map<String, Object> aMapa(Autor a) {
        return Map.of(
                "id", a.getId(),
                "nombre", a.getNombre(),
                "nacionalidad", a.getNacionalidad() == null ? "" : a.getNacionalidad()
        );
    }
}
