# Seguridad, autenticación y aprobación

## Condiciones de acceso

La cuenta requiere simultáneamente `emailVerificado=true` y `estado=APROBADA`. El registro público siempre asigna USER, PENDIENTE y correo sin verificar; campos extra como rol se rechazan.

| Situación | Resultado |
|---|---|
| Credenciales incorrectas o usuario inexistente | Login 401 |
| Correo sin verificar | Login 403 |
| Correo verificado, PENDIENTE | Login 403 |
| APROBADA y correo verificado | Login 200 con JWT |
| RECHAZADA o BLOQUEADA | Login 403 |
| JWT vacío, alterado, vencido o revocado | Recurso protegido 401 |
| USER intenta una función ADMIN | 403 |
| ADMIN intenta aprobar sin correo verificado | 409 |

ADMIN puede pasar una cuenta a PENDIENTE, APROBADA, RECHAZADA o BLOQUEADA con un motivo obligatorio. La aprobación requiere verificación previa. Cada decisión se registra; una nueva revisión puede revertir el rechazo o bloqueo. El administrador no puede modificar su propio estado/rol ni eliminar su propia cuenta.

## Autenticación

Contraseñas protegidas mediante BCrypt con coste 12. Registro exige 10 caracteres, minúscula, mayúscula, número y máximo 72 bytes UTF-8. No hay contraseñas en respuestas ni en texto plano en la tabla usuarios.

JWT HS256 con emisor y audiencia propios del deber 3, sujeto igual al ID, versión de sesión y caducidad de una hora. La clave se genera localmente y no se comparte con el deber 2. Auth comprueba la firma, emisor, audiencia, caducidad, usuario existente, correo, estado y versión actual. El rol se obtiene de la base, no de datos elegidos por el cliente.

Logout y cambios de rol/estado incrementan token_version. Esto invalida todas las sesiones anteriores. Reaprobar no reactiva tokens viejos. Cocina consulta Auth en cada solicitud y aplica el rol y propiedad recibidos.

El endpoint interno exige además X-Internal-Key. La clave interna no se introduce en Postman para las operaciones normales del usuario y no sustituye al JWT.

## Verificación real de correo

Código aleatorio de seis dígitos, protegido con hash BCrypt, caducidad de 10 minutos, máximo cinco intentos y espera de 60 segundos para reenviar. La API no devuelve el código ni lo escribe en sus logs. Los endpoints públicos de autenticación admiten 60 solicitudes por IP cada 15 minutos, con contador local a la instancia.

## Roles y archivos

Todo usuario aprobado puede consultar catálogos, recetas, recomendaciones y descargar los archivos de las recetas. USER solo crea/edita/elimina sus propias recetas y sube/elimina sus archivos. ADMIN gestiona usuarios, catálogos y puede administrar recetas/archivos ajenos.

Archivos PNG, JPEG o PDF de hasta 5 MB, máximo 10 por receta; se verifica la firma inicial de contenido. El nombre interno es aleatorio y la descarga se entrega como adjunto con autenticación. La comprobación de firma no equivale a un antivirus. Para borrar una receta, primero se eliminan sus archivos.

## Alcance local

HTTP se utiliza para localhost en esta práctica. Si se publica en Internet se requiere HTTPS, gestión externa de secretos y límites compartidos entre instancias. No se incluye despliegue público. Las cuentas OAuth en modo de prueba pueden requerir renovar su autorización según las reglas de Google.
