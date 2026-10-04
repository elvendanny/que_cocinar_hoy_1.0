-- Solo para instalaciones nuevas. Si ya preparaste las conexiones, NO necesitas repetirlo.
-- Cambia las claves de ejemplo ANTES de ejecutar en DBeaver como root.
-- No crea tablas: las crea Hibernate al arrancar cada API.
CREATE DATABASE IF NOT EXISTS cocina_cero_d3_auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS cocina_cero_d3_cocina_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'cocina_d3_auth_user'@'localhost' IDENTIFIED BY 'CAMBIA_ESTA_CLAVE_AUTH';
CREATE USER IF NOT EXISTS 'cocina_d3_cocina_user'@'localhost' IDENTIFIED BY 'CAMBIA_ESTA_CLAVE_COCINA';
GRANT ALL PRIVILEGES ON cocina_cero_d3_auth_db.* TO 'cocina_d3_auth_user'@'localhost';
GRANT ALL PRIVILEGES ON cocina_cero_d3_cocina_db.* TO 'cocina_d3_cocina_user'@'localhost';
-- IF NOT EXISTS no cambia contraseñas de usuarios existentes.
