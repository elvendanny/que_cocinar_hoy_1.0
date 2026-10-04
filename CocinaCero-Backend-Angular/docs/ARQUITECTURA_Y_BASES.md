# Arquitectura y estructura de datos

## Aplicaciones

`api-auth` (8081) gestiona usuarios, códigos, revisiones, JWT y envío Gmail. Solo utiliza cocina_cero_d3_auth_db.

`api-cocina` (8082) gestiona categorías, ingredientes, recetas y archivos. Solo utiliza cocina_cero_d3_cocina_db. Para cada petición protegida consulta por HTTP a Auth en `/api/internal/verificar` con el JWT y una clave interna compartida. Si Auth falla, Cocina deniega el acceso; no utiliza datos de permisos antiguos.

`comun` es una biblioteca Java compilada, no un tercer servicio. Contiene Actor, errores y límite de intentos. Cada JAR ejecutable incorpora las dependencias que necesita.

## Carpetas de cada API

- `controller`: rutas HTTP, entradas y respuestas.
- `dto`: cuerpos de las solicitudes y validaciones Jakarta.
- `service`: reglas de registro, aprobación, permisos y negocio.
- `repository`: interfaces Spring Data JPA; heredan las operaciones de JpaRepository.
- `model`: entidades JPA y relaciones que Hibernate almacena en MariaDB.
- `security`: filtros de autenticación y configuración Spring Security.
- `config`: datos iniciales y manejo de conflictos de datos.
- `src/test`: pruebas; los dobles de correo y las bases H2 existen solo aquí, no en producción.

## Base de autenticación

| Tabla | Campos principales y propósito |
|---|---|
| usuarios | id, nombre, email único, password_hash, rol, estado, email_verificado, token_version, creado_en, revisado_en, revisado_por, motivo_revision |
| verificaciones | usuario_id, codigo_hash, enviado_en, vence_en, intentos; una verificación pendiente por usuario |
| revisiones | id, usuario_id, administrador_id, estado_anterior, estado_nuevo, motivo, fecha |

Roles y estados se representan mediante valores de texto validados, no requieren tablas de catálogo. Las revisiones conservan IDs históricos incluso si posteriormente se elimina una cuenta. Los códigos de verificación se eliminan al verificar o eliminar la cuenta; su relación se gestiona en el servicio.

## Base de cocina

| Tabla | Relaciones |
|---|---|
| categorias | Una categoría puede clasificar muchas recetas |
| ingredientes | Catálogo compartido de ingredientes |
| recetas | categoria_id referencia categorias; usuario_id es el ID externo del propietario en Auth |
| receta_ingredientes | Referencias a receta e ingrediente; cantidad, unidad_medida y opcional; combinación receta/ingrediente única |
| archivos | Referencia a receta; nombre original, nombre interno aleatorio, tipo, tamaño y fecha |

Receta e ingrediente tienen una relación muchos a muchos resuelta mediante la entidad de detalle, porque cada combinación tiene cantidad y unidad propias.

No existe una clave foránea entre las dos bases de datos. Cocina utiliza el ID del usuario verificado por Auth. Las recetas iniciales tienen usuario_id=0 como contenido del sistema; solo ADMIN puede modificarlas. Los archivos físicos se guardan en `datos/archivos`, sus metadatos en MariaDB.

Hibernate crea/actualiza el esquema (`ddl-auto=update`) al arrancar sobre las bases preparadas. Es apropiado para esta entrega local; una implantación con datos de producción debe gestionar migraciones versionadas.

No cambies las conexiones a las bases del deber 2: sus estados, tablas y contratos no son los mismos.

## Adaptación web Angular

El módulo `comun` incluye `CorsWeb.java`, usado por ambos servicios para permitir únicamente los orígenes configurados del navegador. Los paquetes Java ahora corresponden a directorios convencionales: por ejemplo `api-cocina/src/main/java/com/cocinacero/cocina/service/RecetasService.java`.

La entidad Receta incorpora `portadaArchivoId` e `imagenDemo`, ambos opcionales. El servicio comprueba que la portada sea una imagen asociada a esa misma receta; no se crea una referencia SQL circular entre recetas y archivos. Al borrar el archivo de portada se limpia la selección. La nueva entidad `CargaInicial` y su tabla `cargas_iniciales` registran la carga demo-web-v1 para no duplicar ni reinsertar datos en cada arranque.

`config/DemoWeb.java` lee `src/main/resources/demo/recetas.json` y añade los datos que faltan. Las ilustraciones se sirven desde el classpath con autenticación. Los nuevos endpoints de paginación usan consultas Spring Data JPA; se mantiene el listado anterior para no romper Postman.

La actualización reutiliza las bases del deber 3; no crea otras ni toca las de los deberes 1 y 2. Se mantienen MEDIUMTEXT para las instrucciones y los dos usuarios de MariaDB con sus bases separadas.
