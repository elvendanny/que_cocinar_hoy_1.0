# Práctica paso a paso en Postman

## Preparación

1. Arranca Auth y Cocina. Importa la colección Deber 3, sin reemplazar la del Deber 2.
2. En la esquina superior de Postman selecciona **No Environment** para evitar que las variables antiguas del deber 2 reemplacen las nuevas.
3. Haz clic en el nombre de la colección y abre **Variables**. Completa sus valores locales:

| Variable | Qué poner |
|---|---|
| adminEmail | El valor app.admin-email de configuracion-local/api-auth.properties |
| adminPassword | El valor app.admin-password del mismo archivo |
| emailReal | Correo que puedas abrir y que no esté registrado en la base del deber 3 |
| passwordReal | Contraseña NUEVA para la aplicación, no la de Gmail. El ejemplo cumple la validación |
| codigoReal | Los seis dígitos del correo de verificación más reciente |

Conserva authUrl en http://localhost:8081 y cocinaUrl en http://localhost:8082. Guarda los cambios. No compartas/exportes los valores privados.

Los scripts guardan IDs y tokens automáticamente como variables de colección. Si una variable está vacía, la solicitud se detendrá con un mensaje indicando cuál completar. Cada POST, PUT y PATCH incluye su Body JSON; los GET, DELETE y logout no lo necesitan. Puedes ver el token configurado en Authorization → Bearer Token.

## Orden de ejecución

Ejecuta solicitud por solicitud; NO uses Run Collection para todo. La verificación de correo, la revisión administrativa y la selección de archivo son pasos manuales.

### 00 — Servicios y ausencia de token

Ambos health deben devolver 200. Recetas sin token debe devolver 401. Explica: el servicio funciona, pero un desconocido no puede entrar.

### 01 — Registro y correo real

- Login antes del registro: 401.
- Registro: 201 y correo real. Se guarda usuarioId. El estado es PENDIENTE.
- Duplicado: 409. Login sin verificar: 403.
- Código incorrecto: 400. No hagas cinco intentos seguidos salvo que quieras probar el bloqueo del código.
- Reenviar: espera 60 segundos desde el envío previo. Copia el código NUEVO a codigoReal.
- Verificar: 200. La cuenta conserva PENDIENTE y emailVerificado cambia a true.
- Login después de verificar, antes de aprobar: 403. Esta es la evidencia central del nuevo requisito.

El código vence en 10 minutos. Se permiten cinco intentos por código. Un reenvío válido reemplaza el anterior. Si se responde 502 por Gmail, revisa docs/CORREO_REAL.md. Un registro cuyo envío falla se revierte; no deja una cuenta parcialmente creada.

### 02 — Revisión ADMIN

Login ADMIN guarda adminToken. Consulta los usuarios y revisa nombre, email, emailVerificado y estado. Envía la aprobación con un motivo. El usuario debe haber verificado el correo; si no, la API responde 409. Consulta el historial: registra administrador, estados anterior/nuevo, motivo y fecha.

### 03 — USER aprobado

Login guarda userToken. Consulta el perfil, cambia tu nombre y consulta recetas. Intentar administrar usuarios o crear categorías con ese token debe devolver 403. Explica la diferencia entre autenticación (quién soy) y autorización (qué puedo hacer).

### 04 — Catálogos

ADMIN crea y actualiza categoría e ingrediente. Los scripts guardan categoriaId e ingredienteId. Todavía NO los elimines: los utilizarás en la receta. Los nombres de demostración deben estar libres; si repetiste una práctica sin limpiar, cambia los nombres en los Body.

### 05 — Recetas y archivos

USER crea su receta y guarda recetaId. Consúltala y actualízala. Las recomendaciones usan IDs de ingredientes disponibles, no una IA ni un inventario.

Eliminar un ingrediente o categoría utilizado debe devolver 409. Para subir un archivo abre Body → form-data, busca la fila **archivo** (tipo File) y selecciona `ejemplos/imagen-prueba.png` o un PDF de hasta 5 MB. No escribas una ruta en JSON ni agregues Content-Type manualmente: Postman genera el encabezado multipart.

La descarga exige token; usa la opción de guardar respuesta de Postman. No es un enlace público. La receta consultada muestra los metadatos de sus archivos. La prueba de receta incompleta espera 400.

### 06 — Propiedad, opcional pero útil

Necesitas otro correo al que puedas acceder. Completa otroEmail, otroPassword y después otroCodigo. Registra, verifica y aprueba ese usuario desde ADMIN. Su login guarda otroToken. Intentar editar la primera receta o borrar su archivo devuelve 403 porque pertenece al primer usuario.

### 07 — Estados

ADMIN bloquea al primer USER. Prueba su token anterior en Cocina: 401. Su login también está denegado (403). Recházalo y prueba el login: 403. Apruébalo de nuevo: el token antiguo sigue revocado. Obtén un token nuevo con login.

El cambio de estado es una decisión administrativa; reaprobar una cuenta antes rechazada está permitido y queda registrado.

### 08 — Completar CRUD y cerrar sesión

Elimina archivo, receta, ingrediente y categoría, en ese orden. Cada DELETE correcto devuelve 204. Consultar la receta eliminada da 404. Logout invalida TODOS los tokens de ese usuario, no solo la copia de Postman. Reutilizar el token anterior en Cocina da 401.

### 09 — Operaciones adicionales manuales

No ejecutes esta carpeta automáticamente. Permite volver a PENDIENTE, cambiar el rol y eliminar la cuenta para repetir la práctica. No puedes modificar tu propio rol/estado ni eliminar al administrador con el que haces la operación.

Eliminar una cuenta Auth no elimina automáticamente sus recetas de Cocina: son bases independientes. Primero limpia sus archivos y recetas. Los IDs no se reinician. El correo puede registrarse otra vez y tendrá un ID nuevo. No se elimina la cuenta Gmail ni la autorización para enviar.

## Si algo no coincide

- 401 inesperado: token vacío, caducado (1 hora), cuenta bloqueada/rechazada/eliminada o sesión revocada. Inicia sesión de nuevo si la cuenta está aprobada.
- 403: revisa qué token usa la solicitud; ADMIN y USER son diferentes.
- 400: revisa Body → raw → JSON, campos y tipos; los IDs y cantidades son números.
- 409 al crear: registro duplicado. 409 al borrar catálogo: hay una receta que lo utiliza.
- 429: espera el intervalo indicado; no pulses Send repetidamente.
- 503 en Cocina: Auth no responde, su URL no coincide o hay un fallo en el servicio.
- ECONNREFUSED: el servicio no está encendido o estás utilizando otro puerto.
- No exportes una colección utilizada con claves reales sin vaciar sus variables.
