USE crediya_db;

CREATE TABLE IF NOT EXISTS empleados (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80),
  documento VARCHAR(30),
  rol VARCHAR(30),
  correo VARCHAR(80),
  salario DECIMAL(10,2)
);

CREATE TABLE IF NOT EXISTS clientes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80),
  documento VARCHAR(30),
  correo VARCHAR(80),
  telefono VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS prestamos (
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

CREATE TABLE IF NOT EXISTS pagos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  prestamo_id INT,
  fecha_pago DATE,
  monto DECIMAL(10,2),
  balance_after DECIMAL(12,2),
  FOREIGN KEY (prestamo_id) REFERENCES prestamos(id)
);

-- ---------------- Datos de ejemplo (solo si la tabla esta vacia) ----------------
INSERT INTO empleados (id, nombre, documento, rol, correo, salario)
SELECT * FROM (
  SELECT 1 AS id, 'Carlos Ramirez' AS nombre, '1012345678' AS documento, 'Asesor de creditos' AS rol, 'carlos.ramirez@crediya.com' AS correo, 2800000 AS salario
  UNION ALL SELECT 2, 'Laura Gomez',  '1023456789', 'Gestor de cobros', 'laura.gomez@crediya.com',  3500000
  UNION ALL SELECT 3, 'Andres Perez', '1034567890', 'Cajero',           'andres.perez@crediya.com', 2200000
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM empleados);

INSERT INTO clientes (id, nombre, documento, correo, telefono)
SELECT * FROM (
  SELECT 1 AS id, 'Maria Fernanda Lopez' AS nombre, '52123456' AS documento, 'maria.lopez@mail.com' AS correo, '3101234567' AS telefono
  UNION ALL SELECT 2, 'Jorge Herrera',  '79234567',   'jorge.herrera@mail.com',  '3119876543'
  UNION ALL SELECT 3, 'Sofia Martinez', '1098765432', 'sofia.martinez@mail.com', '3205558899'
  UNION ALL SELECT 4, 'Diego Castro',   '80345678',   'diego.castro@mail.com',   '3007771122'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM clientes);

INSERT INTO prestamos (id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado,
                       total_interest, total_amount, monthly_installment, outstanding_balance)
SELECT s.id, s.cliente_id, s.empleado_id, s.monto, s.interes, s.cuotas, s.fecha_inicio, s.estado,
       ROUND(s.monto * s.interes / 100 * s.cuotas, 2),
       ROUND(s.monto + s.monto * s.interes / 100 * s.cuotas, 2),
       ROUND((s.monto + s.monto * s.interes / 100 * s.cuotas) / s.cuotas, 2),
       s.saldo
FROM (
  SELECT 1 AS id, 1 AS cliente_id, 1 AS empleado_id, 1000000 AS monto, 2.00 AS interes, 10 AS cuotas, DATE '2026-01-15' AS fecha_inicio, 'OVERDUE' AS estado,  960000 AS saldo
  UNION ALL SELECT 2, 2, 1,  500000, 3.00,  6, DATE '2026-09-01', 'ACTIVE',  590000
  UNION ALL SELECT 3, 3, 3,  300000, 2.50,  4, DATE '2026-03-01', 'PAID',          0
  UNION ALL SELECT 4, 4, 2,  800000, 2.00, 12, DATE '2026-06-10', 'OVERDUE', 992000
) AS s
WHERE NOT EXISTS (SELECT 1 FROM prestamos);

INSERT INTO pagos (id, prestamo_id, fecha_pago, monto, balance_after)
SELECT * FROM (
  SELECT 1 AS id, 1 AS prestamo_id, DATE '2026-02-15' AS fecha_pago, 120000 AS monto, 1080000 AS balance_after
  UNION ALL SELECT 2, 1, DATE '2026-03-15', 120000,  960000
  UNION ALL SELECT 3, 3, DATE '2026-04-01',  82500,  247500
  UNION ALL SELECT 4, 3, DATE '2026-05-01',  82500,  165000
  UNION ALL SELECT 5, 3, DATE '2026-06-01',  82500,   82500
  UNION ALL SELECT 6, 3, DATE '2026-07-01',  82500,       0
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM pagos);

USE crediya_db;
SELECT (SELECT COUNT(*) FROM empleados) AS empleados, (SELECT COUNT(*) FROM clientes) AS clientes,
       (SELECT COUNT(*) FROM prestamos) AS prestamos, (SELECT COUNT(*) FROM pagos) AS pagos;
       
SELECT id, monto, interes, cuotas, estado, total_amount, monthly_installment, outstanding_balance FROM prestamos;

SELECT p.id, p.total_amount, COALESCE(SUM(g.monto),0) AS pagado,
       p.total_amount - COALESCE(SUM(g.monto),0) AS saldo_calculado,
       p.outstanding_balance AS saldo_guardado, p.estado
FROM prestamos p LEFT JOIN pagos g ON g.prestamo_id = p.id
GROUP BY p.id, p.total_amount, p.outstanding_balance, p.estado;