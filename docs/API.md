# Referencia de la API REST

Base URL: `http://localhost:8083/api` · Formato: JSON (`Content-Type: application/json`).

## Libros

### `GET /libros`
Devuelve todos los libros con el nombre de su autor.

```json
[
  { "id": 1, "titulo": "Cien años de soledad", "isbn": "978-0307474728",
    "anioPublicacion": 1967, "paginas": 417, "autorId": 1, "autorNombre": "Gabriel García Márquez" }
]
```

### `GET /libros/{id}`
`200` con el libro, o `404` si no existe.

### `POST /libros`
Cuerpo:

```json
{ "titulo": "Pedro Páramo", "isbn": "978-8437604947", "anioPublicacion": 1955, "paginas": 128, "autorId": 1 }
```

| Campo | Regla |
|---|---|
| `titulo` | obligatorio, máx. 200 |
| `isbn` | obligatorio, máx. 20, **único** |
| `anioPublicacion` | opcional, 1000–2100 |
| `paginas` | opcional, positivo |
| `autorId` | obligatorio, debe existir |

Respuestas: `201` creado · `400` datos inválidos · `404` autor inexistente · `409` ISBN duplicado.

### `PUT /libros/{id}`
Mismo cuerpo que `POST`. Respuestas: `200` · `400` · `404` · `409`.

### `DELETE /libros/{id}`
`204` sin contenido, o `404`.

## Autores

| Método | Ruta | Cuerpo | Respuestas |
|---|---|---|---|
| GET | `/autores` | — | 200 |
| GET | `/autores/{id}` | — | 200 / 404 |
| POST | `/autores` | `{ "nombre": "...", "nacionalidad": "..." }` | 201 / 400 |
| PUT | `/autores/{id}` | igual | 200 / 400 / 404 |
| DELETE | `/autores/{id}` | — | 204 / 404 / **409 si tiene libros** |

## Formato de errores

```json
{ "estado": 400, "mensaje": "Hay datos inválidos en la solicitud",
  "errores": { "titulo": "El título es obligatorio" } }
```

| Código | Cuándo |
|---|---|
| 400 | Falla una validación (`errores` indica el campo) |
| 404 | El recurso (libro o autor) no existe |
| 409 | ISBN duplicado o autor con libros asociados |
