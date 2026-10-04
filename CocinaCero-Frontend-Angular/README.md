# Cocina Cero — Frontend Angular

Frontend del sistema **Cocina Cero**, integrado con el backend Java/Spring Boot del deber anterior.

## Tecnologías

- Angular 21, componentes standalone.
- TypeScript y Reactive Forms.
- HttpClient + interceptor Bearer JWT.
- Guards de autenticación y ADMIN.
- HTML/CSS responsivo sin librerías visuales externas.
- Backend Auth: `http://localhost:8081`.
- Backend Cocina: `http://localhost:8082`.

## Funcionalidades implementadas

- Inicio público con comprobación de estado de ambas API.
- Registro de usuario.
- Verificación por código recibido por correo y reenvío con espera visual.
- Login con JWT y mensajes para cuentas no verificadas/no aprobadas.
- Perfil, edición del nombre y logout real que revoca sesiones.
- Recetario paginado con búsqueda, categoría y dificultad.
- Listado exclusivo de recetas del usuario.
- Detalle completo: foto, ingredientes, preparación, galería y adjuntos.
- Creación y edición de recetas con `FormArray` de ingredientes.
- Carga de PNG/JPEG/PDF mediante `FormData`.
- Portada de receta manual o automática.
- Descarga y eliminación de adjuntos.
- Eliminación completa desde la interfaz respetando la regla del backend de borrar primero los archivos.
- Pantalla **Qué cocinar**, basada en `/api/recomendaciones`.
- ADMIN: usuarios, estados, aprobación, bloqueo, roles e historial.
- ADMIN: CRUD de categorías.
- ADMIN: CRUD y filtros de ingredientes.
- Manejo de errores HTTP 400/401/403/404/409/413/429/502/503.
- Imágenes protegidas obtenidas como `Blob` con JWT y `URL.createObjectURL`.

## Estructura

```text
src/app/
  core/
    guards/
    interceptors/
    models/
    services/
    utils/
  shared/components/
  features/
    auth/
    recipes/
    recommendations/
    profile/
    admin/
```

## Ejecución

En Windows, lo más sencillo es:

1. Iniciar las dos API del backend.
2. Ejecutar `INSTALAR_Y_COMPILAR.cmd` la primera vez.
3. Ejecutar `INICIAR_FRONTEND.cmd`.
4. Abrir `http://127.0.0.1:4200`.

También se puede usar terminal:

```bash
npm install
npm start
```

## Seguridad de integración

Angular solo conoce las URL públicas. La clave interna entre `api-cocina` y `api-auth`, los secretos JWT, las credenciales de MariaDB y la autorización Gmail continúan exclusivamente en el backend. El interceptor únicamente agrega Bearer a solicitudes dirigidas a `localhost:8081` o `localhost:8082`.

La sesión se conserva en `sessionStorage` para facilitar la demostración durante una pestaña del navegador; se borra al cerrar sesión o al recibir un `401` con sesión activa.
