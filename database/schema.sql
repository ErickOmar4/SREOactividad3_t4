-- Script manual para preparar la base de datos (MySQL / MariaDB).
-- Hibernate crea las tablas solo (ddl-auto=update); este script sirve para crear
-- la base y el usuario, y como referencia del esquema resultante.

CREATE DATABASE IF NOT EXISTS biblioteca_act3
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Usuario dedicado (recomendado en el VPS; cambia la contraseña por una segura)
CREATE USER IF NOT EXISTS 'biblioteca_user'@'localhost' IDENTIFIED BY 'CAMBIA_ESTA_CLAVE';
GRANT ALL PRIVILEGES ON biblioteca_act3.* TO 'biblioteca_user'@'localhost';
FLUSH PRIVILEGES;

USE biblioteca_act3;

-- Esquema de referencia (lo genera Hibernate a partir de las entidades)
CREATE TABLE IF NOT EXISTS autor (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(120) NOT NULL,
  nacionalidad VARCHAR(60),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS libro (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  titulo           VARCHAR(200) NOT NULL,
  isbn             VARCHAR(20)  NOT NULL,
  anio_publicacion INT,
  paginas          INT,
  autor_id         BIGINT       NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_libro_isbn (isbn),
  CONSTRAINT fk_libro_autor FOREIGN KEY (autor_id) REFERENCES autor (id)
);

-- Datos de ejemplo (opcional)
INSERT INTO autor (nombre, nacionalidad) VALUES
  ('Gabriel García Márquez', 'Colombiana'),
  ('Octavio Paz', 'Mexicana');

INSERT INTO libro (titulo, isbn, anio_publicacion, paginas, autor_id) VALUES
  ('Cien años de soledad', '978-0307474728', 1967, 417, 1),
  ('El laberinto de la soledad', '978-0802150424', 1950, 224, 2);

-- Consulta que demuestra la relación (JOIN por la llave foránea)
SELECT l.id, l.titulo, a.nombre AS autor
FROM libro l
JOIN autor a ON a.id = l.autor_id;
