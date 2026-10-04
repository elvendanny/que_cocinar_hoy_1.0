# Cocina Cero — Backend preparado para Angular

Esta es una evolución del deber 3 con **Java y Spring Boot**, no Quarkus. Contiene las API Auth (8081) y Cocina (8082), código fuente, pruebas, imágenes de demostración y Postman. **La aplicación Angular se implementará en la siguiente entrega**; este ZIP no contiene todavía una página web.

## 1. Actualizar desde tu deber 3 que ya funciona

1. Detén las dos API anteriores con **Ctrl+C**. Conserva su carpeta completa. En DBeaver puedes guardar un respaldo de las dos bases antes de actualizar.
2. Descomprime este ZIP en una carpeta nueva, por ejemplo `C:\Users\LENOVO\Documents\CocinaCero-Backend-Angular`. Entra hasta ver `IMPORTAR_DEBER3.cmd`, `pom.xml`, `api-auth` y `api-cocina`.
3. Ejecuta **IMPORTAR_DEBER3.cmd**. Cuando pida la ruta anterior, escribe, por ejemplo:
   ```text
   C:\Users\LENOVO\Documents\CocinaCero-Deber3
   ```
   Debe ser la carpeta donde están las configuraciones con las que ya funcionaba el deber 3. El importador copia la configuración privada, el token Gmail y los adjuntos; conserva contraseñas y actualiza las rutas locales. No envía correos ni toca las bases. Si dice que ya hay configuración, no la sobrescribas: si ya importaste, continúa.
4. Mantén MariaDB encendido. Ejecuta **COMPILAR_Y_PROBAR.cmd** y espera **BUILD SUCCESS**. Necesitas JDK 17 o 21 (tu JDK 21 sirve) e Internet para la primera descarga. Las pruebas usan H2 temporal y correo simulado; no modifican tu MariaDB ni envían mensajes.
5. Ejecuta **INICIAR_AUTH.cmd**. Deja abierta la ventana. Después ejecuta **INICIAR_COCINA.cmd** y deja ambas abiertas.
6. En Postman prueba:
   ```text
   GET http://localhost:8081/health
   GET http://localhost:8082/health
   ```
   Ambas deben responder **200**. Después realiza la prueba con sesión del apartado siguiente: health solo confirma que responde el servidor.

Si los `.cmd` no abren con doble clic, abre PowerShell en esta carpeta y ejecuta uno por uno:

```powershell
.\IMPORTAR_DEBER3.cmd
.\COMPILAR_Y_PROBAR.cmd
```

En dos ventanas separadas, desde la misma carpeta:

```powershell
.\INICIAR_AUTH.cmd
```

```powershell
.\INICIAR_COCINA.cmd
```

## 2. Qué pasa con tus datos

Se reutilizan **las mismas dos bases del deber 3**: `cocina_cero_d3_auth_db` y `cocina_cero_d3_cocina_db`. Auth y Cocina siguen teniendo usuarios de conexión y bases separados. No se modifican los deberes 1 y 2. Esta copia y el deber 3 anterior NO son dos instalaciones con datos aislados: apuntan a las mismas bases. No arranques ambas versiones a la vez.

En Cocina, Hibernate agrega `portada_archivo_id`, `imagen_demo` y la tabla `cargas_iniciales`. La carga nueva añade seis recetas y los ingredientes/categorías que falten, una sola vez. No modifica tus recetas previas. Si tenías las tres originales y no hay coincidencias de nombres, verás **nueve recetas**. Si ya tienes una receta con un nombre de demostración, se conserva la tuya. Borrar una receta de muestra después no hace que reaparezca al reiniciar.

Las ilustraciones de demostración van dentro del proyecto y no necesitan un sitio externo. Las fotos que tú subas se guardan en `datos/archivos`. Conserva esta carpeta junto con el respaldo de MariaDB.

## 3. Prueba rápida antes de Angular

1. Importa `postman/CocinaCero-Backend-Angular.postman_collection.json`.
2. Selecciona **No environment / Sin entorno** para no reutilizar variables antiguas. Abre las variables de esta colección y escribe tu `adminEmail` y `adminPassword` actuales. No son las credenciales de Gmail.
3. Abre **00A - Comprobar backend Angular**. Ejecuta sus solicitudes en orden, de una en una. El login guarda el JWT; las siguientes solicitudes guardan los IDs automáticamente.
4. En **09 Subir imagen**, elige en Body → form-data → archivo el archivo `ejemplos/imagen-prueba.png` de esta copia. No escribas a mano el encabezado Content-Type del multipart.
5. La carpeta comprueba CORS, opciones de formularios, recetas paginadas, creación/edición de una receta temporal, foto, portada y eliminación. Al terminar no quedan esa receta ni su archivo de prueba.
6. Para el correo real y los permisos de USER conserva el recorrido de las carpetas **01 a 09** de la colección. Usa un correo nuevo: registro → código real → verificación → aprobación ADMIN → login USER. Tu cuenta ya registrada se mantiene: un segundo registro devuelve 409, no indica una avería.

Guía completa del recorrido anterior: `docs/GUIA_POSTMAN.md`. Contrato para la próxima web: `docs/CONTRATO_ANGULAR.md`. Propuesta de pantallas y operaciones: `docs/PROPUESTA_FUNCIONAL_ANGULAR.md`.

## 4. Cambios incluidos

- CORS para `http://localhost:4200` y `http://127.0.0.1:4200`, sin desproteger recursos.
- Filtros de usuarios por texto, estado y verificación para ADMIN.
- Recetas paginadas, filtro de recetas propias y datos de imagen principal.
- Subida PNG/JPEG/PDF existente; lectura de imágenes para Angular y selección de portada.
- Validación del contenido de las imágenes, propiedad y tamaño; límite de 5 MB y diez archivos por receta.
- Opciones de formularios consultables desde la API.
- Seis recetas con pasos e ingredientes y siete ilustraciones originales (una genérica).
- Carpetas Java organizadas siguiendo los paquetes convencionales.
- Pruebas automatizadas de las funciones anteriores y las nuevas.

No hay que instalar Angular ni Node para probar estas API Java. La utilidad opcional `herramientas-correo` solo sirve para renovar OAuth si Google invalida el permiso.

## 5. Problemas habituales

- **Puerto ocupado:** detén la API anterior, no cambies puertos al azar.
- **Access denied de MariaDB:** revisa la configuración privada importada; no recrees ni borres las bases.
- **401:** inicia sesión otra vez y comprueba que la colección tenga el token. El JWT dura una hora; cerrar sesión, bloquear o cambiar roles lo revoca.
- **403:** el usuario aún no verificó su correo/no está APROBADA, o no tiene el rol/propiedad exigido.
- **502 al registrar o reenviar:** revisa Internet y la autorización Gmail. `docs/CORREO_REAL.md` explica la renovación; un token presente no garantiza que Google siga aceptándolo.
- **409 al borrar receta:** primero elimina sus archivos; al borrar categoría/ingrediente, primero desvincúlalo de las recetas.
- **CORS:** el origen de la web debe coincidir exactamente con los permitidos. Si más adelante cambia el puerto, configura `app.cors.allowed-origins` en ambos archivos privados y reinicia.
- **Hay 200 en health pero falla una receta:** health no comprueba todas las operaciones. Copia el error de esa solicitud y el final de la consola de Cocina.

## 6. Entrega y alcance de las comprobaciones

Usa **EMPACAR_ENTREGA.cmd** para crear un ZIP de código sin claves, tokens, adjuntos privados ni compilaciones. No comprimas manualmente toda tu carpeta después de importar las credenciales.

El resultado automatizado está en `docs/RESULTADOS_PRUEBAS.json`. Se comprueban bases H2 en modo MariaDB, el DDL del dialecto MariaDB y comunicación HTTP real entre los servicios; Gmail se sustituye en pruebas. La conexión con tu MariaDB y un nuevo envío real deben confirmarse en tu equipo siguiendo esta guía.
