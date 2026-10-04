# Propuesta funcional — Cocina Cero con Angular

**Estudiante:** ____________________  **Asignatura:** ____________________  **Fecha:** ____________________

## Objetivo y alcance

Construir una aplicación web con Angular que consuma el backend Java/Spring Boot del deber 3 y permita gestionar recetas e ingredientes desde pantallas. Se conserva la verificación por correo real, aprobación por ADMIN, autenticación JWT y separación de Auth/Cocina con bases MariaDB independientes. La referencia a Quarkus en una parte del enunciado no corresponde al backend seleccionado: esta propuesta utiliza Spring Boot, como la API previa.

Esta entrega prepara el backend y documenta lo que se implementará en Angular. No presenta una interfaz Angular como ya construida ni incluye la investigación posterior de usabilidad, accesibilidad o UX.

## Recursos revisados

Auth dispone de usuarios, verificación por correo, inicio/cierre de sesión, perfil, roles y revisiones administrativas. Cocina dispone de categorías, ingredientes, recetas, relación receta-ingredientes, archivos e identificación de recetas con ingredientes disponibles. El catálogo de opciones evita valores de formulario incompatibles con las validaciones.

Se pueden consultar recetas, ingredientes y categorías con una cuenta APROBADA. USER puede crear recetas y editar/borrar sus contenidos. ADMIN gestiona usuarios y catálogos y puede editar todas las recetas. Visitantes solo acceden a registro/verificación/login y a la información introductoria que cree el frontend; el recetario permanece protegido.

## Vistas, funcionalidades y operaciones propuestas

| Vista Angular propuesta | Funciones de la pantalla | Operaciones sobre API | Acceso |
|---|---|---|---|
| Inicio `/` | Presentación de Cocina Cero, acceso a registro y login | No requiere consulta protegida | Público |
| Registro `/registro` | Nombre, correo, contraseña, envío del código | POST Auth `/api/auth/registro` | Público |
| Verificación `/verificar` | Introducir código recibido, reenviar, mostrar espera de aprobación | POST Auth `/api/auth/verificar`, `/api/auth/reenviar` | Público |
| Login `/login` | Entrar y explicar si falta verificar/aprobar | POST Auth `/api/auth/login` | Público |
| Recetas `/recetas` | Tarjetas con imagen, búsqueda, categoría, dificultad, paginación | GET Cocina `/api/recetas/pagina`, `/api/categorias`, `/api/opciones`; imágenes con JWT | USER/ADMIN aprobados |
| Detalle `/recetas/:id` | Pasos, porciones, tiempo, cantidades, galería y descargar adjuntos | GET Cocina `/api/recetas/{id}`, `/api/archivos/{id}` y rutas de imagen | USER/ADMIN aprobados |
| Mis recetas `/mis-recetas` | Listado propio y botones de editar/borrar | GET Cocina `/api/recetas/pagina?mias=true`; DELETE de receta/archivos | Propietario o ADMIN |
| Formulario `/recetas/nueva` y `/recetas/:id/editar` | Crear/editar, añadir líneas de ingredientes, subir imágenes, elegir portada | GET catálogos/opciones; POST/PUT recetas; POST archivos; PATCH portada; DELETE archivos | Propietario o ADMIN para editar |
| Qué cocinar `/que-cocinar` | Seleccionar ingredientes disponibles y ver porcentaje de coincidencias | GET `/api/ingredientes`; GET `/api/recomendaciones?ingredientes=1,2` | USER/ADMIN aprobados |
| Perfil `/perfil` | Consultar usuario, editar nombre y cerrar sesión | GET/PATCH Auth `/api/auth/me`; POST `/api/auth/logout` | Usuario autenticado |
| Usuarios `/admin/usuarios` | Buscar pendientes, revisar verificación, aprobar/rechazar/bloquear, cambiar rol e historial | GET usuarios/roles/revisiones; PATCH estado/rol | ADMIN |
| Categorías `/admin/categorias` | Listar, crear, editar, eliminar si no está en uso | GET/POST/PUT/DELETE Cocina `/api/categorias` y `/{id}` | ADMIN |
| Ingredientes `/admin/ingredientes` | Buscar, filtrar tipo, crear, editar, eliminar si no está en uso | GET/POST/PUT/DELETE Cocina `/api/ingredientes` y `/{id}` | ADMIN |

La recomendación compara ingredientes requeridos, excluye opcionales del cálculo y ordena por coincidencias. No utiliza inteligencia artificial ni garantiza restricciones nutricionales.

## Relación entre las capas

Angular enviará JSON y FormData con HttpClient. Auth comprobará contraseñas/códigos hash, emitirá JWT y consultará su base. Cocina recibirá el JWT, llamará por HTTP a Auth con una credencial interna del servidor y consultará su propia base. Así, un bloqueo o cierre de sesión se refleja también en Cocina.

Las imágenes de usuarios viven en disco y sus metadatos en MariaDB. Angular las consulta con JWT y las presenta como blobs. Las ilustraciones de ejemplo van dentro del JAR. Ninguna pantalla se conecta directamente a MariaDB ni recibe las claves internas o de Gmail.

Carpetas previstas del frontend: `core` (servicios HTTP, interceptor, sesión y guards), `shared` (componentes reutilizables), `features/auth`, `features/recetas`, `features/perfil`, `features/admin` y `assets`. La estructura definitiva y los componentes Angular se crearán en la siguiente fase; HTML/CSS acompañarán a los componentes TypeScript.

## Criterios de demostración de la futura web

1. Registrar un correo nuevo proporcionado durante la exposición y recibir el código real.
2. Verificar el correo y demostrar que aún no se puede acceder hasta la aprobación de ADMIN.
3. Aprobar, iniciar sesión y ver el recetario con imágenes.
4. Crear una receta con ingredientes, subir foto, consultar detalle, editarla y eliminarla con sus adjuntos.
5. Comprobar que USER no administra usuarios/catálogos ni modifica recetas ajenas.
6. Buscar recetas, consultar recomendaciones y gestionar catálogos como ADMIN.
7. Bloquear una cuenta y comprobar que un token ya emitido deja de servir; probar logout.
8. Mostrar que cambios del navegador se guardan en las bases MariaDB mediante las API.

El recorrido técnico exacto, códigos de respuesta y cuerpos JSON se encuentran en `CONTRATO_ANGULAR.md`, `ENDPOINTS.md` y la colección Postman. Las capturas de Angular se agregarán después de implementar la interfaz; las pruebas actuales corresponden al backend.
