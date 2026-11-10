package com.biblioteca.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte excepciones en respuestas JSON con el código HTTP correcto. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Map<String, Object>> conflicto(ConflictoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException ex) {
        return respuesta(HttpStatus.CONFLICT,
                "La operación viola una restricción de la base de datos (dato duplicado o relación en uso)", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", errores);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String mensaje, Object errores) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", estado.value());
        cuerpo.put("mensaje", mensaje);
        if (errores != null) {
            cuerpo.put("errores", errores);
        }
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
