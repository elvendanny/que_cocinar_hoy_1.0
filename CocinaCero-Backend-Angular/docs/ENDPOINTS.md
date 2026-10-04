# Endpoints del deber 3

Auth: http://localhost:8081. Cocina: http://localhost:8082. Enviar JSON salvo carga multipart.

| Método | Servicio y ruta | Autenticación |
|---|---|---|
| GET | `Auth/health` | Público |
| GET | `Cocina/health` | Público |
| GET | `Cocina/api/recetas` | JWT; rol/propiedad según operación |
| POST | `Auth/api/auth/login` | Público |
| POST | `Auth/api/auth/registro` | Público |
| POST | `Auth/api/auth/verificar` | Público |
| POST | `Auth/api/auth/reenviar` | Público |
| GET | `Auth/api/usuarios` | JWT; rol/propiedad según operación |
| GET | `Auth/api/usuarios/{{usuarioId}}` | JWT; rol/propiedad según operación |
| PATCH | `Auth/api/usuarios/{{usuarioId}}/estado` | JWT; rol/propiedad según operación |
| GET | `Auth/api/usuarios/{{usuarioId}}/revisiones` | JWT; rol/propiedad según operación |
| GET | `Auth/api/roles` | JWT; rol/propiedad según operación |
| GET | `Auth/api/auth/me` | JWT; rol/propiedad según operación |
| PATCH | `Auth/api/auth/me` | JWT; rol/propiedad según operación |
| POST | `Cocina/api/categorias` | JWT; rol/propiedad según operación |
| GET | `Cocina/api/categorias` | JWT; rol/propiedad según operación |
| GET | `Cocina/api/categorias/{{categoriaId}}` | JWT; rol/propiedad según operación |
| PUT | `Cocina/api/categorias/{{categoriaId}}` | JWT; rol/propiedad según operación |
| GET | `Cocina/api/ingredientes` | JWT; rol/propiedad según operación |
| POST | `Cocina/api/ingredientes` | JWT; rol/propiedad según operación |
| GET | `Cocina/api/ingredientes/{{ingredienteId}}` | JWT; rol/propiedad según operación |
| PUT | `Cocina/api/ingredientes/{{ingredienteId}}` | JWT; rol/propiedad según operación |
| POST | `Cocina/api/recetas` | JWT; rol/propiedad según operación |
| GET | `Cocina/api/recetas/{{recetaId}}` | JWT; rol/propiedad según operación |
| PUT | `Cocina/api/recetas/{{recetaId}}` | JWT; rol/propiedad según operación |
| GET | `Cocina/api/recomendaciones` | JWT; rol/propiedad según operación |
| DELETE | `Cocina/api/ingredientes/{{ingredienteId}}` | JWT; rol/propiedad según operación |
| DELETE | `Cocina/api/categorias/{{categoriaId}}` | JWT; rol/propiedad según operación |
| POST | `Cocina/api/recetas/{{recetaId}}/archivos` | JWT; rol/propiedad según operación |
| GET | `Cocina/api/archivos/{{archivoId}}` | JWT; rol/propiedad según operación |
| PATCH | `Auth/api/usuarios/{{otroId}}/estado` | JWT; rol/propiedad según operación |
| DELETE | `Cocina/api/archivos/{{archivoId}}` | JWT; rol/propiedad según operación |
| DELETE | `Cocina/api/recetas/{{recetaId}}` | JWT; rol/propiedad según operación |
| POST | `Auth/api/auth/logout` | JWT; rol/propiedad según operación |
| PATCH | `Auth/api/usuarios/{{usuarioId}}/rol` | JWT; rol/propiedad según operación |
| DELETE | `Auth/api/usuarios/{{usuarioId}}` | JWT; rol/propiedad según operación |

Además: `GET Auth/api/internal/verificar` exige JWT y X-Internal-Key; es consumido por Cocina, no por el usuario directamente. Los marcadores usuarioId, categoriaId, ingredienteId, recetaId y archivoId representan IDs numéricos reales. otroId es otro usuario en la misma ruta /usuarios/{id}.

Las rutas /usuarios y /roles requieren ADMIN. POST/PUT/DELETE de categorías e ingredientes requieren ADMIN. Modificaciones de recetas y archivos requieren propietario o ADMIN. Consultas de cocina y perfil requieren usuario aprobado.

GET /usuarios acepta page (desde 1) y limit (1 a 100; predeterminado 50). GET /recetas admite texto, categoriaId y dificultad. GET /ingredientes admite texto y tipo. GET /recomendaciones requiere ingredientes separados por coma.

POST /auth/registro: nombre, email, password. POST /auth/login: email, password. POST /auth/verificar: email, codigo. POST /auth/reenviar: email. PATCH /auth/me: nombre. PATCH /usuarios/{id}/estado: estado, motivo. PATCH /usuarios/{id}/rol: rol.

Categorias: nombre, descripcion opcional. Ingredientes: nombre, descripcion opcional, tipo. Recetas: nombre, descripcion, instrucciones, tiempoPreparacion, porciones, dificultad, categoriaId e ingredientes con ingredienteId, cantidad, unidadMedida y opcional. PUT envía el cuerpo completo.

Creación: 201. Consulta/actualización: 200. Eliminación: 204. Validación: 400. Autenticación: 401. Permisos: 403. No encontrado: 404. Duplicado/relaciones: 409. Límites: 429. Fallo Gmail: 502. Auth no disponible para Cocina: 503.

Los ejemplos completos están en los Body de cada solicitud de Postman.

## Extensión para Angular

| Método | Servicio/ruta | Acceso |
|---|---|---|
| GET | Cocina `/api/opciones` | USER/ADMIN aprobado |
| GET | Cocina `/api/recetas/pagina` | USER/ADMIN aprobado |
| PATCH | Cocina `/api/recetas/{id}/portada` | Propietario o ADMIN |
| GET | Cocina `/api/archivos/{id}/imagen` | USER/ADMIN aprobado |
| GET | Cocina `/api/imagenes-demo/{nombre}` | USER/ADMIN aprobado |

`/api/recetas` y `/api/recetas/pagina` admiten `mias=true`, además de `texto`, `categoriaId` y `dificultad`. La consulta paginada acepta page≥1 y limit 1–50, por defecto 12. `/api/usuarios` añade `texto`, `estado` y `emailVerificado`. Portada recibe `{ "archivoId": ID_NUMERICO }` o null para selección automática. Las imágenes devuelven bytes, no JSON. Consulta `CONTRATO_ANGULAR.md` para tipos, respuestas, tratamiento de imágenes protegidas y flujo de sesión.
