-- ============================
--   TABLA USUARIO
-- ============================
CREATE TABLE usuario (
  id_usuario SERIAL PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  email VARCHAR(200) NOT NULL UNIQUE,
  password_hash VARCHAR(200) NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
  intentos_fallidos INT NOT NULL DEFAULT 0,
  rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
  CHECK (estado IN ('ACTIVO','BLOQUEADO','INACTIVO')),
  CHECK (rol IN ('CLIENTE','ADMIN'))
);

CREATE INDEX idx_usuario_email ON usuario(email);

-- ============================
--   TABLA ADMINISTRADOR
-- ============================
CREATE TABLE administrador (
  id_admin SERIAL PRIMARY KEY,
  id_usuario INT NOT NULL REFERENCES usuario(id_usuario)
);

-- ============================
--   TABLA CUENTA
-- ============================
CREATE TABLE cuenta (
  id_cuenta SERIAL PRIMARY KEY,
  numero_cuenta VARCHAR(50) NOT NULL UNIQUE,
  saldo NUMERIC(12,2) NOT NULL DEFAULT 0,
  tipo VARCHAR(20) NOT NULL,
  id_usuario INT NOT NULL REFERENCES usuario(id_usuario),
  CHECK (tipo IN ('AHORROS','CORRIENTE','EMPRESARIAL'))
);

CREATE INDEX idx_cuenta_usuario ON cuenta(id_usuario);

-- ============================
--   TABLA TRANSACCION
-- ============================
CREATE TABLE transaccion (
  id_transaccion SERIAL PRIMARY KEY,
  tipo VARCHAR(20) NOT NULL,
  monto NUMERIC(12,2) NOT NULL,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado VARCHAR(20) NOT NULL DEFAULT 'EXITOSO',
  id_cuenta INT NOT NULL REFERENCES cuenta(id_cuenta),
  CHECK (tipo IN ('RETIRO','DEPOSITO','TRANSFERENCIA','CONSULTA')),
  CHECK (estado IN ('EXITOSO','FALLIDO'))
);

CREATE INDEX idx_transaccion_cuenta ON transaccion(id_cuenta);

-- ============================
--   TABLA ERROR LOG
-- ============================
CREATE TABLE errorlog (
  id_error SERIAL PRIMARY KEY,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  descripcion TEXT NOT NULL,
  id_usuario INT REFERENCES usuario(id_usuario)
);

-- ============================
--   TABLA ACCESO LOG
-- ============================
CREATE TABLE accesolog (
  id_log SERIAL PRIMARY KEY,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resultado VARCHAR(100) NOT NULL,
  id_usuario INT REFERENCES usuario(id_usuario)
);
