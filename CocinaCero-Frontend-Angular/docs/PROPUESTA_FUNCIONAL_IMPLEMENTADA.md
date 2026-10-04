# Propuesta funcional implementada — Cocina Cero

## Objetivo

Integrar mediante Angular el backend Java/Spring Boot del bloque anterior para permitir que un usuario interactúe desde una interfaz web con la autenticación, recetas, ingredientes, categorías, archivos y administración almacenados en MariaDB a través de la API REST.

## Vistas implementadas

| Vista | Funcionalidad principal | API utilizada |
|---|---|---|
| Inicio | Presentación y estado de ambos servicios | `GET /health` en Auth y Cocina |
| Registro | Crear cuenta y enviar código real | `POST /api/auth/registro` |
| Verificación | Validar código y reenviarlo | `POST /api/auth/verificar`, `/reenviar` |
| Login | Obtener JWT de una cuenta aprobada | `POST /api/auth/login` |
| Recetas | Buscar, filtrar y paginar | `GET /api/recetas/pagina` |
| Mis recetas | Consultar solo contenido propio | `GET /api/recetas/pagina?mias=true` |
| Detalle | Ingredientes, pasos, imágenes y adjuntos | `GET /api/recetas/{id}`, archivos e imágenes |
| Nueva/Editar receta | CRUD, ingredientes, adjuntos y portada | `POST/PUT /api/recetas`, carga de archivos y portada |
| Qué cocinar | Recomendar según ingredientes disponibles | `GET /api/recomendaciones` |
| Perfil | Consultar/editar nombre y cerrar sesión | `GET/PATCH /api/auth/me`, `POST /logout` |
| Usuarios ADMIN | Filtrar, aprobar, bloquear, rol e historial | `/api/usuarios/**` |
| Categorías ADMIN | CRUD de categorías | `/api/categorias/**` |
| Ingredientes ADMIN | CRUD, búsqueda y filtro | `/api/ingredientes/**` |

## Organización de capas

1. **Angular** presenta formularios y vistas, realiza peticiones HTTP y conserva la sesión de la pestaña.
2. **API Auth Spring Boot** registra usuarios, envía códigos, valida credenciales, emite JWT y administra roles/estados.
3. **API Cocina Spring Boot** gestiona recetas, catálogos, recomendaciones y archivos.
4. **MariaDB** conserva los datos; Angular nunca se conecta directamente a la base.
5. **Gmail** continúa siendo utilizado exclusivamente por el backend para el código de verificación.

## Permisos

- Visitante: inicio, registro, verificación y login.
- USER aprobado: consulta del recetario, creación de recetas y edición/eliminación de contenido propio.
- ADMIN: todo lo anterior más gestión de usuarios y catálogos, además de administración de recetas de otros usuarios.

Los guards de Angular organizan la navegación, pero la seguridad real permanece en Java, que vuelve a validar JWT, rol y propiedad en cada operación protegida.

## Alcance de esta entrega

Esta implementación se concentra en la integración funcional Angular + API REST + MariaDB. No incorpora como trabajo teórico la investigación posterior de usabilidad, accesibilidad, UX/UI o diseño responsivo solicitada para una actividad futura, aunque la interfaz ya se adapta visualmente a distintos tamaños de pantalla.
