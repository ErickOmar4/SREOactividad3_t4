# Biblioteca · CRUD Libro–Autor con Spring Boot, JPA y MySQL

CRUD completo con una relación **uno a muchos** (`Autor 1 ──── N Libro`), construido con Spring Boot y MySQL, con un frontend minimalista (HTML + CSS + JavaScript) servido por el mismo proyecto.

> Proyecto académico — Actividad 3. Pensado también como material de aprendizaje: cada tecnología está explicada y la carpeta `docs/` incluye guías extra.

## Contenido

1. [Tecnologías utilizadas](#1-tecnologías-utilizadas)
2. [Arquitectura](#2-arquitectura)
3. [Modelo de datos](#3-modelo-de-datos)
4. [Requisitos previos](#4-requisitos-previos)
5. [Cómo ejecutar el proyecto](#5-cómo-ejecutar-el-proyecto)
6. [API REST](#6-api-rest)
7. [Pruebas con Postman / Bruno](#7-pruebas-con-postman--bruno)
8. [Despliegue en VPS](#8-despliegue-en-vps)
9. [Estructura del repositorio](#9-estructura-del-repositorio)
10. [Capturas de pantalla](#10-capturas-de-pantalla)
11. [Documentación adicional](#11-documentación-adicional)

---

## 1. Tecnologías utilizadas

| Tecnología | ¿Para qué sirve? | ¿Cómo se configuró / usó aquí? |
|---|---|---|
| **Java 17+** | Lenguaje y plataforma de ejecución del backend. | Se fija en `pom.xml` con `<java.version>17</java.version>`. |
| **Spring Boot** | Framework que arranca una app web con configuración mínima (servidor embebido, autoconfiguración). | Se genera en [start.spring.io](https://start.spring.io) y se hereda en el `pom.xml` con `spring-boot-starter-parent`. |
| **Spring Web** (`spring-boot-starter-web`) | Crea controladores REST y sirve archivos estáticos. | Clases `@RestController` en `controller/`. El frontend se sirve desde `static/`. |
| **Spring Data JPA** (`spring-boot-starter-data-jpa`) | Capa de persistencia: mapea clases Java a tablas y genera consultas. | Entidades `@Entity` en `model/`; interfaces `JpaRepository` en `repository/`. |
| **Hibernate** | Implementación de JPA que traduce objetos a SQL. | Viene con el starter. `ddl-auto=update` crea/ajusta las tablas. |
| **MySQL / MariaDB** | Base de datos relacional donde persisten los datos. | Conexión en `application.properties` mediante variables de entorno. |
| **MySQL Connector/J** (`mysql-connector-j`) | Driver JDBC para conectar Java con MySQL. | Dependencia con scope `runtime`. |
| **Bean Validation** (`spring-boot-starter-validation`) | Valida datos de entrada con anotaciones (`@NotBlank`, `@Min`…). | En los DTO (`LibroRequest`, `AutorRequest`) junto con `@Valid` en el controlador. |
| **Maven** | Gestiona dependencias y compila el `.jar`. | `backend/pom.xml`. Copia `frontend/` dentro del `.jar` con `maven-resources-plugin`. |
| **HTML + CSS + JavaScript (fetch)** | Interfaz para usar el CRUD sin herramientas extra. | `frontend/`. Sin frameworks; consume la API con `fetch`. |
| **Postman / Bruno** | Probar los endpoints directamente. | Ver [sección 7](#7-pruebas-con-postman--bruno). |
| **Git / GitHub** | Control de versiones y entrega. | `.gitignore` excluye `target/`, `.env` y secretos. |

### Decisiones de diseño

- **DTOs (`record`)**: el cliente nunca recibe ni envía la entidad directamente; así se evitan ciclos JSON en la relación y se controla qué se expone.
- **Capas separadas**: el controlador solo recibe/responde; la lógica de negocio (ISBN único, autor existente, no borrar autor con libros) vive en el **service**.
- **`JOIN FETCH`** en `LibroRepository` para traer el autor junto al libro y evitar el problema N+1.
- **Credenciales fuera del código**: `application.properties` lee `DB_URL`, `DB_USER`, `DB_PASSWORD` y `SERVER_PORT` de variables de entorno.

---

## 2. Arquitectura

Arquitectura en capas dentro de un único proyecto Spring Boot, con el frontend en una carpeta separada que se empaqueta en el mismo `.jar`.

```
┌────────────────────────┐        HTTP / JSON        ┌───────────────────────────────────────────┐
│  FRONTEND (navegador)  │ ───────────────────────►  │  BACKEND (Spring Boot, puerto 8083)       │
│  index.html + CSS + JS │ ◄───────────────────────  │                                           │
└────────────────────────┘                           │  Controller  →  Service  →  Repository    │
                                                     │  (REST)         (negocio)   (Spring Data) │
                                                     └───────────────────────┬───────────────────┘
                                                                             │ JPA / Hibernate / JDBC
                                                                             ▼
                                                                    ┌──────────────────┐
                                                                    │  MySQL (tablas   │
                                                                    │  autor y libro)  │
                                                                    └──────────────────┘
```

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Presentación (API) | `controller` | Define endpoints, valida con `@Valid`, devuelve códigos HTTP. |
| Negocio | `service` | Reglas del dominio y transacciones (`@Transactional`). |
| Persistencia | `repository` | Acceso a datos con `JpaRepository` y consultas JPQL. |
| Dominio | `model` | Entidades JPA y relación `@OneToMany` / `@ManyToOne`. |
| Transferencia | `dto` | Objetos de entrada/salida de la API. |
| Transversal | `exception`, `config` | Manejo global de errores (404, 409, 400) y CORS. |

**Flujo de ejemplo (crear un libro):** el formulario envía `POST /api/libros` → `LibroController` valida el JSON → `LibroService` comprueba ISBN único y busca el `Autor` → `LibroRepository.save()` inserta la fila con `autor_id` → la respuesta incluye `autorNombre`.

---

## 3. Modelo de datos

```
┌──────────────┐ 1          N ┌─────────────────────┐
│    autor     │──────────────│        libro        │
├──────────────┤              ├─────────────────────┤
│ id (PK)      │              │ id (PK)             │
│ nombre       │              │ titulo              │
│ nacionalidad │              │ isbn (UNIQUE)       │
└──────────────┘              │ anio_publicacion    │
                              │ paginas             │
                              │ autor_id (FK)  ─────┼──► autor.id
                              └─────────────────────┘
```

- `Libro` es el **dueño** de la relación (`@ManyToOne` + `@JoinColumn(name = "autor_id")`), por eso la llave foránea vive en `libro`.
- `Autor` espeja la relación con `@OneToMany(mappedBy = "autor")`.
- Un autor con libros **no se puede eliminar** (responde `409 Conflict`).

El script `database/schema.sql` contiene el esquema de referencia y datos de ejemplo.

---

## 4. Requisitos previos

| Herramienta | Versión recomendada | Cómo obtenerla |
|---|---|---|
| JDK | 17 o superior | [adoptium.net](https://adoptium.net) · Ubuntu: `sudo apt install openjdk-17-jdk` |
| Maven | 3.9+ (o usar el wrapper de tu IDE) | [maven.apache.org](https://maven.apache.org/download.cgi) · Ubuntu: `sudo apt install maven` |
| MySQL | 8.0+ (o MariaDB 10.6+) | [dev.mysql.com](https://dev.mysql.com/downloads/) · Ubuntu: `sudo apt install mysql-server` |
| Git | cualquiera reciente | [git-scm.com](https://git-scm.com) |
| Navegador | Chrome, Firefox, Edge… | — |
| Postman o Bruno *(opcional)* | última | [postman.com](https://www.postman.com) · [usebruno.com](https://www.usebruno.com) |

Verifica las versiones:

```bash
java -version
mvn -version
mysql --version
```

> **Versión de Spring Boot:** el `pom.xml` usa `3.5.7`. Si en tu equipo usas otra (por ejemplo 4.0.x), cambia solo la etiqueta `<version>` del `parent`. El código usa `jakarta.*` y es compatible con ambas.

---

## 5. Cómo ejecutar el proyecto

### Paso 1 · Clonar

```bash
git clone https://github.com/TU_USUARIO/TUSINICIALESact3_t4.git
cd TUSINICIALESact3_t4
```

### Paso 2 · Preparar la base de datos

```bash
Get-Service -Name MySQL80          # debe aparecer "active (running)"
sudo mysql < database/schema.sql     # crea la base, el usuario y datos de ejemplo
```

*(Alternativa: la URL por defecto incluye `createDatabaseIfNotExist=true`, así que con un usuario con permisos también se crea sola.)*

### Paso 3 · Configurar variables de entorno

Linux / macOS:

```bash
export DB_URL="jdbc:mysql://localhost:3306/biblioteca_act3?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true"
export DB_USER="biblioteca_user"
export DB_PASSWORD="la_clave_que_pusiste"
export SERVER_PORT=8083
```

Windows (PowerShell):

```powershell
$env:DB_USER="biblioteca_user"; $env:DB_PASSWORD="la_clave_que_pusiste"
```

Hay una plantilla en [`.env.example`](.env.example).

### Paso 4 · Ejecutar

```bash
cd backend
mvn spring-boot:run
```

### Paso 5 · Usar la aplicación

Abre **http://localhost:8083** en el navegador. Primero crea un autor y luego un libro.

### Generar el `.jar`

```bash
cd backend
mvn clean package -DskipTests
java -jar target/biblioteca-1.0.0.jar
```

### Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Access denied for user` | Usuario/clave incorrectos | Revisa `DB_USER` y `DB_PASSWORD`. |
| `Communications link failure` | MySQL apagado o URL errónea | `sudo systemctl start mysql` y revisa `DB_URL`. |
| `Port 8083 was already in use` | Otro proceso usa el puerto | Cambia `SERVER_PORT`. |
| El frontend dice "No se pudo conectar" | Abriste `index.html` con doble clic | Entra por `http://localhost:8083` o define `window.API_BASE`. |

---

## 6. API REST

Base: `/api`. Detalle completo con ejemplos en [`docs/API.md`](docs/API.md).

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/libros` | Lista libros (con nombre del autor) | 200 |
| GET | `/api/libros/{id}` | Obtiene un libro | 200 / 404 |
| POST | `/api/libros` | Crea un libro | 201 / 400 / 404 / 409 |
| PUT | `/api/libros/{id}` | Actualiza un libro | 200 / 400 / 404 / 409 |
| DELETE | `/api/libros/{id}` | Elimina un libro | 204 / 404 |
| GET/POST/PUT/DELETE | `/api/autores[/{id}]` | CRUD de autores | ver API.md |

Ejemplo — crear un libro:

```bash
curl -X POST http://localhost:8083/api/libros \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Pedro Páramo","isbn":"978-8437604947","anioPublicacion":1955,"paginas":128,"autorId":1}'
```

Respuesta:

```json
{ "id": 3, "titulo": "Pedro Páramo", "isbn": "978-8437604947",
  "anioPublicacion": 1955, "paginas": 128, "autorId": 1, "autorNombre": "Gabriel García Márquez" }
```

---

## 7. Pruebas con Postman / Bruno

1. Crea una colección con la URL base `http://localhost:8083/api`.
2. Crea un autor (`POST /autores`), luego un libro con ese `autorId`.
3. Verifica con `GET /libros` que aparece `autorNombre`.
4. Prueba errores: ISBN repetido (409), campos vacíos (400), `autorId` inexistente (404), borrar un autor con libros (409).
5. Confirma la persistencia en MySQL:

```sql
SELECT l.id, l.titulo, a.nombre AS autor FROM libro l JOIN autor a ON a.id = l.autor_id;
```

---

## 8. Despliegue en VPS

Resumen (guía paso a paso en [`docs/DESPLIEGUE_VPS.md`](docs/DESPLIEGUE_VPS.md)):

1. Instalar JDK y MySQL en el VPS; crear base y usuario con `database/schema.sql`.
2. `mvn clean package -DskipTests` y subir el `.jar` con `scp` a una **carpeta nueva**.
3. Definir las variables de entorno y ejecutar en un **puerto nuevo** (ej. `8083`), sin tocar las instancias anteriores.
4. Abrir el puerto en el firewall y probar desde fuera: `http://IP_DEL_VPS:8083`.

---

## 9. Estructura del repositorio

```
TUSINICIALESact3_t4/
├── backend/                      # Proyecto Spring Boot (Maven)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/biblioteca/
│       │   ├── BibliotecaApplication.java
│       │   ├── config/           # CorsConfig
│       │   ├── controller/       # AutorController, LibroController
│       │   ├── dto/              # LibroRequest, LibroResponse, AutorRequest
│       │   ├── exception/        # Manejo global de errores
│       │   ├── model/            # Autor, Libro (entidades JPA)
│       │   ├── repository/       # AutorRepository, LibroRepository
│       │   └── service/          # AutorService, LibroService
│       └── resources/application.properties
├── frontend/                     # Interfaz (se copia dentro del .jar al compilar)
│   ├── index.html
│   ├── css/styles.css
│   └── js/app.js
├── database/schema.sql           # Esquema y datos de ejemplo
├── docs/                         # Guías adicionales y capturas
├── .env.example
├── .gitignore
└── README.md
```

---


## 11. Documentación adicional

- [`docs/API.md`](docs/API.md) — referencia de endpoints, cuerpos y códigos de error.
- [`docs/DESPLIEGUE_VPS.md`](docs/DESPLIEGUE_VPS.md) — despliegue paso a paso, servicio `systemd` y firewall.
- `bitacora_rehacer_proyecto.xlsx` — bitácora para reconstruir el proyecto desde cero, por temas.

## Licencia

Uso académico.
