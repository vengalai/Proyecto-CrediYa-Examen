-- =====================================================
-- CrediYa - MySQL schema + sample data
-- Run:  mysql -u root -p < sql/crediya_schema.sql
-- =====================================================
DROP DATABASE IF EXISTS crediya_db;
CREATE DATABASE crediya_db;
USE crediya_db;

CREATE TABLE empleados (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80),
  documento VARCHAR(30),
  rol VARCHAR(30),
  correo VARCHAR(80),
  salario DECIMAL(10,2)
);

CREATE TABLE clientes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80),
  documento VARCHAR(30),
  correo VARCHAR(80),
  telefono VARCHAR(20)
);

CREATE TABLE prestamos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id INT,
  empleado_id INT,
  monto DECIMAL(12,2),
  interes DECIMAL(5,2),
  cuotas INT,
  fecha_inicio DATE,
  estado VARCHAR(20),
  total_interest DECIMAL(12,2),
  total_amount DECIMAL(12,2),
  monthly_installment DECIMAL(12,2),
  outstanding_balance DECIMAL(12,2),
  FOREIGN KEY (cliente_id) REFERENCES clientes(id),
  FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);

CREATE TABLE pagos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  prestamo_id INT,
  fecha_pago DATE,
  monto DECIMAL(10,2),
  balance_after DECIMAL(12,2),
  FOREIGN KEY (prestamo_id) REFERENCES prestamos(id)
);

-- ---------------- Datos de ejemplo ----------------
INSERT INTO empleados (nombre, documento, rol, correo, salario) VALUES
('Carlos Ramirez', '1012345678', 'Asesor de creditos', 'carlos.ramirez@crediya.com', 2800000),
('Laura Gomez',    '1023456789', 'Gestor de cobros',    'laura.gomez@crediya.com',    3500000),
('Andres Perez',   '1034567890', 'Cajero',              'andres.perez@crediya.com',   2200000);

INSERT INTO clientes (nombre, documento, correo, telefono) VALUES
('Maria Fernanda Lopez', '52123456',   'maria.lopez@mail.com',   '3101234567'),
('Jorge Herrera',        '79234567',   'jorge.herrera@mail.com', '3119876543'),
('Sofia Martinez',       '1098765432', 'sofia.martinez@mail.com','3205558899'),
('Diego Castro',         '80345678',   'diego.castro@mail.com',  '3007771122');

-- Prestamo 1: vencido (2 cuotas pagadas de 10, inicio ene 2026)
-- Prestamo 2: activo (inicio sep 2026)
-- Prestamo 3: pagado completamente
-- Prestamo 4: vencido (sin pagos, inicio jun 2026)
INSERT INTO prestamos (cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado) VALUES
(1, 1, 1000000, 2.00, 10, '2026-01-15', 'OVERDUE'),
(2, 1,  500000, 3.00,  6, '2026-09-01', 'ACTIVE'),
(3, 3,  300000, 2.50,  4, '2026-03-01', 'PAID'),
(4, 2,  800000, 2.00, 12, '2026-06-10', 'OVERDUE');

INSERT INTO pagos (prestamo_id, fecha_pago, monto) VALUES
(1, '2026-02-15', 120000),
(1, '2026-03-15', 120000),
(3, '2026-04-01',  82500),
(3, '2026-05-01',  82500),
(3, '2026-06-01',  82500),
(3, '2026-07-01',  82500);