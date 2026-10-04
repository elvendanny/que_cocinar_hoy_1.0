# Verificación realizada y evidencias para la entrega

Compilación Maven `clean package` finalizada con código 0. Se ejecutaron 12 pruebas agrupadas en 5 clases, sin fallos ni omisiones. Cada prueba contiene múltiples peticiones y comprobaciones.

| Clase | Pruebas | Cobertura |
|---|---|---|
| SeguridadIntegrationTest | 5 | Registro, hash de contraseña, duplicado, cuenta pendiente, código incorrecto/caducado, límite de cinco intentos, reenvío, aprobación, rechazo, bloqueo, roles, revocación, eliminación y rollback ante fallo de correo; CORS y filtros administrativos sin hashes |
| CocinaIntegrationTest | 4 | CRUD de catálogos/recetas, permisos, propiedad, recomendaciones, conflictos de relaciones, carga/descarga/eliminación de archivos y validaciones; CORS, imágenes, portadas y paginación de recetas propias |
| EsquemaMariaDbTest | 1 | Generación del DDL con el dialecto MariaDB: instrucciones MEDIUMTEXT y relaciones con recetas |
| MicroserviciosIntegrationTest | 1 | Dos servidores HTTP reales, dos bases separadas, JWT real, aprobación, acceso entre servicios, bloqueo y logout con rechazo posterior del token |

| DemoWebIntegrationTest | 1 | Actualización sobre datos previos: preserva ediciones y recetas del usuario, evita duplicados y no resucita recetas eliminadas |

Las bases de estas pruebas son H2 temporales en modo MariaDB, no las bases del usuario. Se sustituye Gmail por un capturador de códigos exclusivo de pruebas. Por tanto no se afirma haber verificado aquí la conexión a tu instalación MariaDB ni la entrega real de mensajes. Ambas comprobaciones se completan en tu equipo al configurar y arrancar.

El importador Java se ejecutó con datos ficticios y rutas con espacios: copia adjuntos y configuración, conserva el origen y rechaza una segunda importación sin sobrescribir. La colección Postman contiene 84 solicitudes; se revisaron los JSON y scripts. No se ejecutó Postman en tu equipo ni se enviaron correos desde esta preparación.

## Capturas sugeridas en tu presentación

1. Registro 201 y correo recibido, ocultando códigos ya usados si no son necesarios.
2. Cuenta con emailVerificado=true y estado PENDIENTE, seguida del login denegado 403.
3. ADMIN aprueba la cuenta y consulta el historial con su motivo.
4. Login 200 y acceso a recetas 200; no muestres completo el JWT.
5. USER recibe 403 al crear categoría y ADMIN obtiene 201 con el mismo JSON.
6. CRUD de receta: creación 201, consulta/actualización 200, eliminación 204 y consulta final 404.
7. Carga de archivo 201 y descarga correcta.
8. Bloqueo y rechazo inmediato del token en Cocina; logout también invalida el token.

En DBeaver puedes mostrar ambas bases y sus tablas. No incluyas configuracion-local ni credenciales en las capturas o el ZIP de entrega.

Para repetir las pruebas automatizadas: `COMPILAR_Y_PROBAR.cmd`. Se impide cargar los archivos privados de configuración en las pruebas y se utilizan exclusivamente bases temporales.

## Comprobaciones específicas para la web

La carpeta 00A de Postman contiene el recorrido rápido y los ejemplos preparados. Comprueba OPTIONS con Origin permitido, login, opciones, paginación, creación/edición de receta, subida de imagen, selección de portada, lectura protegida y eliminación. CORS se ha comprobado en filtros Spring reales mediante MockMvc; la integración Auth–Cocina utiliza además dos servidores HTTP. No se ha ejecutado una interfaz Angular, que corresponde a la siguiente fase.
