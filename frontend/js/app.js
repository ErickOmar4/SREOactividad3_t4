/* Frontend del CRUD Biblioteca: JavaScript puro (sin frameworks) consumiendo la API REST. */

// Si el frontend lo sirve Spring (mismo origen) la base queda vacía.
// Si abres index.html por separado, define window.API_BASE = "http://localhost:8083" antes de este script.
const API = (window.API_BASE || "").replace(/\/$/, "");

const $ = (id) => document.getElementById(id);
$("api-url").textContent = (API || location.origin) + "/api";

/* ---------- utilidades ---------- */
async function peticion(ruta, opciones = {}) {
  const resp = await fetch(API + ruta, {
    headers: { "Content-Type": "application/json" },
    ...opciones,
  });
  if (resp.status === 204) return null;
  const datos = await resp.json().catch(() => null);
  if (!resp.ok) {
    const error = new Error((datos && datos.mensaje) || "Error " + resp.status);
    error.errores = datos && datos.errores;
    throw error;
  }
  return datos;
}

let temporizador;
function avisar(texto, esError = false) {
  const caja = $("aviso");
  caja.textContent = texto;
  caja.className = "aviso" + (esError ? " aviso--error" : "");
  caja.hidden = false;
  clearTimeout(temporizador);
  temporizador = setTimeout(() => (caja.hidden = true), 4500);
}

function celda(texto, clase) {
  const td = document.createElement("td");
  td.textContent = texto ?? "";
  if (clase) td.className = clase;
  return td;
}

function boton(texto, clase, accion) {
  const b = document.createElement("button");
  b.type = "button";
  b.textContent = texto;
  b.className = "boton boton--chico " + clase;
  b.addEventListener("click", accion);
  return b;
}

function limpiarErrores(form) {
  form.querySelectorAll(".error-campo").forEach((e) => e.remove());
  form.querySelectorAll(".invalido").forEach((e) => e.classList.remove("invalido"));
}

function mostrarErrores(form, errores, mapa) {
  limpiarErrores(form);
  Object.entries(errores || {}).forEach(([campo, mensaje]) => {
    const input = $(mapa[campo]);
    if (!input) return;
    input.classList.add("invalido");
    const span = document.createElement("span");
    span.className = "error-campo";
    span.textContent = mensaje;
    input.parentElement.appendChild(span);
  });
}

/* ---------- pestañas ---------- */
document.querySelectorAll(".pestana").forEach((p) =>
  p.addEventListener("click", () => {
    document.querySelectorAll(".pestana").forEach((x) => x.classList.toggle("activa", x === p));
    document.querySelectorAll(".vista").forEach((v) => (v.hidden = v.id !== "vista-" + p.dataset.vista));
  })
);

/* ---------- AUTORES ---------- */
const mapaAutor = { nombre: "autor-nombre", nacionalidad: "autor-nacionalidad" };

async function cargarAutores() {
  const autores = await peticion("/api/autores");
  const cuerpo = $("tabla-autores");
  cuerpo.replaceChildren();
  if (!autores.length) {
    const tr = document.createElement("tr");
    const td = celda("Aún no hay autores. Registra uno para poder crear libros.", "vacio");
    td.colSpan = 4;
    tr.appendChild(td);
    cuerpo.appendChild(tr);
  }
  autores.forEach((a) => {
    const tr = document.createElement("tr");
    tr.append(celda(a.id), celda(a.nombre), celda(a.nacionalidad));
    const acc = celda("", "celda-acciones");
    acc.append(
      boton("Editar", "", () => editarAutor(a)),
      " ",
      boton("Eliminar", "boton--peligro", () => eliminarAutor(a))
    );
    tr.appendChild(acc);
    cuerpo.appendChild(tr);
  });

  // Rellena el <select> del formulario de libros
  const select = $("libro-autor");
  const actual = select.value;
  select.replaceChildren(new Option("Selecciona un autor…", ""));
  autores.forEach((a) => select.appendChild(new Option(a.nombre, a.id)));
  select.value = actual;
}

function editarAutor(a) {
  $("autor-id").value = a.id;
  $("autor-nombre").value = a.nombre;
  $("autor-nacionalidad").value = a.nacionalidad;
  $("titulo-form-autor").textContent = "Editar autor #" + a.id;
  $("btn-cancelar-autor").hidden = false;
  $("autor-nombre").focus();
}

function reiniciarFormAutor() {
  $("form-autor").reset();
  $("autor-id").value = "";
  $("titulo-form-autor").textContent = "Nuevo autor";
  $("btn-cancelar-autor").hidden = true;
  limpiarErrores($("form-autor"));
}
$("btn-cancelar-autor").addEventListener("click", reiniciarFormAutor);

$("form-autor").addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = $("autor-id").value;
  const cuerpo = JSON.stringify({
    nombre: $("autor-nombre").value,
    nacionalidad: $("autor-nacionalidad").value,
  });
  try {
    await peticion(id ? "/api/autores/" + id : "/api/autores", { method: id ? "PUT" : "POST", body: cuerpo });
    avisar(id ? "Autor actualizado" : "Autor creado");
    reiniciarFormAutor();
    await cargarAutores();
    await cargarLibros();
  } catch (err) {
    mostrarErrores($("form-autor"), err.errores, mapaAutor);
    avisar(err.message, true);
  }
});

async function eliminarAutor(a) {
  if (!confirm(`¿Eliminar al autor "${a.nombre}"?`)) return;
  try {
    await peticion("/api/autores/" + a.id, { method: "DELETE" });
    avisar("Autor eliminado");
    await cargarAutores();
  } catch (err) {
    avisar(err.message, true);
  }
}

/* ---------- LIBROS ---------- */
const mapaLibro = {
  titulo: "libro-titulo",
  isbn: "libro-isbn",
  anioPublicacion: "libro-anio",
  paginas: "libro-paginas",
  autorId: "libro-autor",
};

async function cargarLibros() {
  const libros = await peticion("/api/libros");
  const cuerpo = $("tabla-libros");
  cuerpo.replaceChildren();
  if (!libros.length) {
    const tr = document.createElement("tr");
    const td = celda("Aún no hay libros registrados.", "vacio");
    td.colSpan = 7;
    tr.appendChild(td);
    cuerpo.appendChild(tr);
  }
  libros.forEach((l) => {
    const tr = document.createElement("tr");
    const tdAutor = document.createElement("td");
    const chip = document.createElement("span");
    chip.className = "etiqueta";
    chip.textContent = l.autorNombre; // la relación: se muestra el NOMBRE, no solo el id
    tdAutor.appendChild(chip);

    tr.append(celda(l.id), celda(l.titulo), celda(l.isbn), celda(l.anioPublicacion), celda(l.paginas), tdAutor);
    const acc = celda("", "celda-acciones");
    acc.append(
      boton("Editar", "", () => editarLibro(l)),
      " ",
      boton("Eliminar", "boton--peligro", () => eliminarLibro(l))
    );
    tr.appendChild(acc);
    cuerpo.appendChild(tr);
  });
}

function editarLibro(l) {
  $("libro-id").value = l.id;
  $("libro-titulo").value = l.titulo;
  $("libro-isbn").value = l.isbn;
  $("libro-anio").value = l.anioPublicacion ?? "";
  $("libro-paginas").value = l.paginas ?? "";
  $("libro-autor").value = l.autorId;
  $("titulo-form-libro").textContent = "Editar libro #" + l.id;
  $("btn-cancelar-libro").hidden = false;
  $("libro-titulo").focus();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function reiniciarFormLibro() {
  $("form-libro").reset();
  $("libro-id").value = "";
  $("titulo-form-libro").textContent = "Nuevo libro";
  $("btn-cancelar-libro").hidden = true;
  limpiarErrores($("form-libro"));
}
$("btn-cancelar-libro").addEventListener("click", reiniciarFormLibro);

$("form-libro").addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = $("libro-id").value;
  const numero = (v) => (v === "" ? null : Number(v));
  const cuerpo = JSON.stringify({
    titulo: $("libro-titulo").value,
    isbn: $("libro-isbn").value,
    anioPublicacion: numero($("libro-anio").value),
    paginas: numero($("libro-paginas").value),
    autorId: numero($("libro-autor").value),
  });
  try {
    await peticion(id ? "/api/libros/" + id : "/api/libros", { method: id ? "PUT" : "POST", body: cuerpo });
    avisar(id ? "Libro actualizado" : "Libro creado");
    reiniciarFormLibro();
    await cargarLibros();
  } catch (err) {
    mostrarErrores($("form-libro"), err.errores, mapaLibro);
    avisar(err.message, true);
  }
});

async function eliminarLibro(l) {
  if (!confirm(`¿Eliminar el libro "${l.titulo}"?`)) return;
  try {
    await peticion("/api/libros/" + l.id, { method: "DELETE" });
    avisar("Libro eliminado");
    await cargarLibros();
  } catch (err) {
    avisar(err.message, true);
  }
}

/* ---------- arranque ---------- */
(async () => {
  try {
    await cargarAutores();
    await cargarLibros();
  } catch (err) {
    avisar("No se pudo conectar con la API: " + err.message, true);
  }
})();
