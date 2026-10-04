# Prueba funcional sugerida

## 1. Servicios

- Abrir `/` y comprobar que Auth 8081 y Cocina 8082 aparecen conectados.

## 2. Cuenta nueva

1. Registro con correo real y contraseña válida.
2. Verificar el código de seis dígitos.
3. Intentar login antes de aprobación: debe mostrarse el `403` del backend.
4. Entrar como ADMIN, abrir Usuarios y aprobar la cuenta verificada.
5. Login con USER: debe acceder al recetario.

## 3. Recetas

1. Buscar y filtrar recetas.
2. Abrir detalle y comprobar que la imagen se carga autenticada.
3. Crear receta con al menos un ingrediente.
4. Adjuntar una imagen PNG/JPEG y, opcionalmente, un PDF.
5. Abrir detalle, descargar archivo, editar datos y cambiar portada.
6. Eliminar la receta: la interfaz elimina primero sus adjuntos y luego la receta.

## 4. Recomendaciones

- Abrir **Qué cocinar**, marcar ingredientes y comprobar que las recetas aparecen ordenadas por porcentaje de coincidencia.

## 5. Permisos

- USER no debe ver las rutas ADMIN.
- USER no puede editar recetas de otro usuario.
- ADMIN puede gestionar recetas y catálogos.
- Cambiar estado/rol de otro usuario revoca sus tokens anteriores.
- Logout revoca la sesión en el backend.

## 6. Evidencia para exposición

Tomar capturas de: inicio, login, recetario, formulario de receta, detalle con foto, recomendaciones y panel ADMIN. Para demostrar persistencia, refrescar el listado después de crear/editar datos y, si el docente lo pide, mostrar las tablas correspondientes en MariaDB/DBeaver.
