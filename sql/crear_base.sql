-- ============================================================
-- Script de base de datos del servicio de registro e inicio de sesión
-- Ejecutar una sola vez en MySQL antes de iniciar el servicio.
-- ============================================================

-- Crea la base de datos si no existe
CREATE DATABASE IF NOT EXISTS autenticacion_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE autenticacion_db;

-- Tabla de usuarios.
-- La contraseña NUNCA se guarda en texto plano: se guarda su "hash" y la "sal" (salt)
-- usada para calcularlo.
CREATE TABLE IF NOT EXISTS usuarios (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    usuario        VARCHAR(50)  NOT NULL UNIQUE,  -- nombre de usuario (único)
    clave_hash     VARCHAR(255) NOT NULL,         -- hash de la contraseña
    salt           VARCHAR(64)  NOT NULL,         -- sal aleatoria del usuario
    fecha_registro TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
