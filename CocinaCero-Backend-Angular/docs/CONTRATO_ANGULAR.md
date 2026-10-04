# Contrato de integración con Angular

## Direcciones y autenticación

- Auth: `http://localhost:8081`; Cocina: `http://localhost:8082`.
- Angular local: `http://localhost:4200` o `http://127.0.0.1:4200`.
- JSON con `Content-Type: application/json`; archivos con `FormData`, campo `archivo`.
- Tras login, usar `Authorization: Bearer <token>` en las solicitudes protegidas de ambas API. El interceptor de Angular debe limitarse a estas bases URL; no enviará el JWT a terceros.
- No colocar JWT en la URL. `X-Internal-Key`, secretos JWT, claves de MariaDB y OAuth Gmail pertenecen exclusivamente al servidor.
- CORS admite GET, POST, PUT, PATCH, DELETE y OPTIONS. El preflight se responde antes del filtro de autenticación. No se usan cookies de sesión ni `withCredentials`. El endpoint interno no acepta llamadas CORS.
- Conservar el JWT en memoria durante la sesión y volver a iniciar sesión al recargar es una opción segura y simple para la primera web. Los guards mejoran navegación; los permisos reales siempre se verifican en Java.

## Flujo de cuentas

1. POST Auth `/api/auth/registro`: `{"nombre":"Ana Pérez","email":"correo-real@ejemplo.com","password":"CocinaSegura2026!"}`. 201 con `usuario` y mensaje; USER/PENDIENTE, correo sin verificar. Usar un correo real controlado por el presentador.
2. POST Auth `/api/auth/verificar`: `{"email":"correo-real@ejemplo.com","codigo":"123456"}`. Código real de seis dígitos, como string para conservar ceros. 200; sigue PENDIENTE.
3. POST Auth `/api/auth/reenviar`: `{"email":"correo-real@ejemplo.com"}`. Esperar al menos 60 s. Cada código caduca a los diez minutos y admite cinco intentos; reenviar sustituye el anterior.
4. ADMIN inicia sesión y consulta `/api/usuarios?estado=PENDIENTE&emailVerificado=true`. PATCH `/api/usuarios/{id}/estado`: `{"estado":"APROBADA","motivo":"Correo verificado y revisión aprobada"}`.
5. POST Auth `/api/auth/login`: `{"email":"correo-real@ejemplo.com","password":"CocinaSegura2026!"}`. 200: `token`, `tipo`, `expiraEn:3600`, `usuario`. Con credenciales correctas pero sin verificar/aprobar responde 403 y mensaje específico. No hay JWT de usuario pendiente.
6. GET Auth `/api/auth/me` obtiene el usuario de la sesión; PATCH con `{"nombre":"Ana actualizada"}` cambia su nombre. POST `/api/auth/logout` revoca sus sesiones.

El administrador inicial ya existe en tu base. El importador conserva sus credenciales; no crea otra cuenta ni cambia su contraseña. No se ha añadido recuperación de contraseña ni inicio con Google: OAuth Gmail solo autoriza el envío de códigos.

## Recetario y formularios

Se conservan los endpoints y cuerpos del deber 3. `ENDPOINTS.md` contiene la lista completa. Añadidos:

| Método / ruta Cocina | Respuesta y uso |
|---|---|
| GET `/api/opciones` | Dificultades, tipos de ingrediente, unidades y restricciones de archivos para selectores |
| GET `/api/recetas/pagina?page=1&limit=12` | `{datos:[],total,page,limit,paginas}`; límite 1–50 |
| GET `/api/recetas/pagina?mias=true` | Solo recetas cuyo propietario es el usuario del JWT, también para ADMIN |
| GET `/api/recetas?mias=true` | Lista sin paginar, compatible con la colección anterior |
| PATCH `/api/recetas/{id}/portada` | `{"archivoId":123}`; archivo de esa receta PNG/JPEG; propietario o ADMIN |
| GET `/api/archivos/{id}/imagen` | Bytes PNG/JPEG; JWT; disposición inline, no devuelve PDF |
| GET `/api/imagenes-demo/{nombre}` | Ilustración empaquetada, con JWT; usar la URL devuelta por la receta |

Ambas consultas de recetas admiten `texto`, `categoriaId` y `dificultad` (FACIL/MEDIA/DIFICIL). Se ordenan por ID descendente. `mias` no acepta un ID de usuario arbitrario: se calcula con el JWT. Las consultas siguen permitiendo ver las recetas de otros usuarios aprobados; editar/borrar requiere propiedad o ADMIN. Las recetas demo tienen `usuarioId:0` y las administra ADMIN.

Cada receta añade `creadoEn` e `imagenPrincipal`. Ejemplo:

```json
{"origen":"ARCHIVO","archivoId":123,"tipo":"image/png","url":"/api/archivos/123/imagen"}
```

Si no hay foto subida, se devuelve `origen:DEMO`, tipo PNG y URL de ilustración (sin archivoId). Precedencia: portada seleccionada → primera imagen subida por ID → ilustración demo o genérica. `{"archivoId":null}` quita la selección explícita y activa esa precedencia; no elimina fotos.

**Para mostrar imágenes protegidas**, Angular deberá pedir `cocinaUrl + imagenPrincipal.url` con HttpClient, `responseType:'blob'` y JWT. Crear `URL.createObjectURL(blob)` y asignarlo al `src` de `<img>`; revocar la URL al cambiar imagen o destruir el componente. No usar directamente la ruta protegida en `<img src>` porque el navegador no agrega el Bearer. El frontend podrá conservar los blobs en memoria mientras dure la sesión.

Subida: POST `/api/recetas/{id}/archivos`, `FormData` con campo `archivo`. Máximo 5 MB y diez archivos por receta. PNG/JPEG: se comprueba firma, lectura y máximo veinte megapíxeles. PDF: se comprueba firma reconocida y solo se descarga como adjunto. Una foto no pertenece a otra receta; el servidor rechaza usarla como portada ajena. Al borrar la portada, se elige otra imagen o la ilustración automáticamente.

Creación/edición completa de receta (reemplazar IDs por los devueltos en categorías/ingredientes):

```json
{
  "nombre":"Mi tortilla de práctica",
  "descripcion":"Receta creada desde el formulario web",
  "instrucciones":"1. Batir los huevos.\n2. Cocinar en sartén hasta cuajar.\n3. Servir caliente.",
  "tiempoPreparacion":15,
  "porciones":2,
  "dificultad":"FACIL",
  "categoriaId":1,
  "ingredientes":[
    {"ingredienteId":3,"cantidad":2,"unidadMedida":"UNIDADES","opcional":false}
  ]
}
```

POST `/api/recetas` crea (201). PUT `/api/recetas/{id}` actualiza (200). DELETE devuelve 204, sin JSON. El frontend confirma antes de borrar; si hay adjuntos, debe eliminarlos primero. No se añadió borrado en cascada oculto.

## Panel de administración

GET Auth `/api/usuarios` conserva `{datos,total,page,limit}` y añade filtros opcionales `texto`, `estado` y `emailVerificado=true|false`. `page` empieza en 1; `limit` es 1–100. El buscador consulta nombre/correo. Todos los `/api/usuarios/**` y `/api/roles` requieren ADMIN.

Aprobar requiere correo verificado. Cambiar estado o rol invalida JWT anteriores. ADMIN no puede cambiar su propio estado/rol ni borrarse. La interfaz mostrará revisiones con GET `/api/usuarios/{id}/revisiones`. Borrar una cuenta no borra sus recetas porque pertenecen a otro servicio: para la primera web se prioriza bloquear la cuenta; si se ofrece eliminación definitiva, antes deben revisarse sus contenidos.

## Errores que debe mostrar la interfaz

- 400: `error` y, para validación de DTO, `detalles:[{campo,mensaje}]`; mostrar el error junto al campo cuando exista.
- 401: sesión ausente, vencida o revocada; limpiar sesión local y volver a login.
- 403: operación sin permisos o cuenta no habilitada al iniciar sesión.
- 404: recurso eliminado/no existente; volver al listado y actualizarlo.
- 409: duplicado, dependencia o conflicto; conservar formulario y explicar el mensaje.
- 413: archivo supera tamaño de petición; 400 también puede indicar archivo inválido.
- 429: límite de peticiones o reenvío demasiado pronto.
- 502: no se pudo enviar Gmail; no presentar registro como finalizado si la API devolvió error.
- 503: Auth no disponible para Cocina; ofrecer reintentar.

Mostrar estados de carga, lista vacía y errores; evitar envíos duplicados deshabilitando el botón mientras se espera. Es comportamiento funcional de la web, sin investigación UX adicional.
