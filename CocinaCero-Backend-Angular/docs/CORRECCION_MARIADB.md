# Corrección del error 1074 de MariaDB

El campo instrucciones de Receta usaba VARCHAR(20000), incompatible con el límite de MariaDB y utf8mb4. Se cambia su mapeo a MEDIUMTEXT y se conserva la validación de 20.000 caracteres en el DTO.

Archivo: api-cocina/src/main/java/com/cocinacero/cocina/model/Receta.java.

Antes: @Column(nullable = false, length = 20000)
Después: @Column(nullable = false, columnDefinition = "MEDIUMTEXT")

Para actualizar una instalación existente, cambia únicamente esa anotación, guarda, detén las API, ejecuta COMPILAR_Y_PROBAR.cmd y vuelve a iniciarlas. No borres las bases ni repitas CONFIGURAR.cmd. Hibernate actualizará el esquema al arrancar. El fallo inicial dejó la tabla recetas sin crear; los errores de sus claves foráneas fueron secundarios.

Se añade una prueba que genera DDL con el dialecto de MariaDB y comprueba el tipo de almacenamiento y las relaciones. Esta prueba no sustituye una ejecución sobre un servidor MariaDB real.
