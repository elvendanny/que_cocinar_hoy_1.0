# Integración con CocinaCero-Backend-Angular

El frontend fue construido contra el contrato real incluido en el backend entregado.

## Auth — 8081

- `POST /api/auth/registro`
- `POST /api/auth/verificar`
- `POST /api/auth/reenviar`
- `POST /api/auth/login`
- `GET/PATCH /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/usuarios`
- `PATCH /api/usuarios/{id}/estado`
- `PATCH /api/usuarios/{id}/rol`
- `GET /api/usuarios/{id}/revisiones`

## Cocina — 8082

- `GET /api/opciones`
- `GET /api/categorias`
- CRUD ADMIN de categorías.
- `GET /api/ingredientes`
- CRUD ADMIN de ingredientes.
- `GET /api/recetas/pagina`
- `GET/POST/PUT/DELETE /api/recetas...`
- `POST /api/recetas/{id}/archivos`
- `PATCH /api/recetas/{id}/portada`
- `GET /api/archivos/{id}`
- `GET /api/archivos/{id}/imagen`
- `DELETE /api/archivos/{id}`
- `GET /api/recomendaciones`

Las imágenes no se colocan directamente en `<img src>` porque requieren Bearer JWT. `ProtectedImageComponent` las solicita con `HttpClient` como `Blob`, crea una URL local temporal y la revoca al destruir el componente.
