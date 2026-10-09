# CrediYa - Sistema de Gestion de Prestamos y Cobros

Aplicacion de consola en Java para gestionar prestamos y cobros de cartera.

## Caracteristicas
| Modulo | Descripcion |
|---|---|
| Empleados | Registrar y consultar empleados (ID, nombre, documento, rol, correo, salario) |
| Clientes | Registrar y listar clientes, ver cliente con todos sus prestamos |
| Prestamos | Crear prestamos (cliente + empleado), interes simple, total, cuota mensual, estado |
| Pagos | Registrar pagos (transaccion), actualizar saldo, historial de pagos, recibo |
| Reportes | Prestamos activos, vencidos, clientes morosos, resumen de cartera (Lambdas + Streams) |

## Estructura del proyecto
```
CrediYa/
├── sql/crediya_schema.sql        # tablas + datos de ejemplo
├── config.properties             # url / usuario / password de BD
├── lib/                          # driver JDBC de MySQL (.jar)
├── data/                         # archivos de texto generados en tiempo de ejecucion
├── docs/class-diagram.md         # UML (Mermaid, se renderiza en GitHub)
├── docs/sample-output/           # ejemplos de archivos de texto generados
├── run.sh / run.bat              # compilar y ejecutar
└── src/com/crediya/
    ├── Main.java
    ├── model/      Person (abstracta), Employee, Client, Loan, Payment, LoanStatus
    ├── dao/        GenericDAO<T>, EmployeeDAO, ClientDAO, LoanDAO, PaymentDAO
    ├── service/    Employee/Client/Loan/Payment/ReportService, InterestCalculator
    ├── util/       DBConnection, FileManager, InputHelper, Validator, MoneyUtil
    ├── view/       Menu (abstracta), MainMenu y un menu por modulo
    └── exception/  CrediYaException
```

## Configuracion
1. Instalar **JDK 17+** y **MySQL 8**.
2. Crear la base de datos: `mysql -u root -p < sql/crediya_schema.sql`
3. Descargar **MySQL Connector/J** (https://dev.mysql.com/downloads/connector/j/, "Platform Independent") y copiar el `.jar` en `lib/`.
4. Editar `config.properties` con tu usuario y password de MySQL.
5. Ejecutar: `./run.sh` (Linux/macOS) o `run.bat` (Windows).

## Reglas de negocio
- **Interes (simple):** `interes = monto x tasa/100 x cuotas`; `total = monto + interes`; `cuota = total / cuotas`. La tasa es mensual.
- **Proxima fecha de pago:** `fecha_inicio + (cuotas cubiertas + 1) meses`, donde cuotas cubiertas = `floor(total pagado / cuota)`.
- **Estado:** saldo = 0 -> `PAID`; proxima fecha de pago en el pasado -> `OVERDUE`; de lo contrario `ACTIVE`. Se actualiza al iniciar, al listar prestamos y en reportes.
- Un cliente con un prestamo vencido no puede recibir uno nuevo.
- Un pago no puede exceder el saldo pendiente. El INSERT del pago y el UPDATE del prestamo corren en **una transaccion**.
- Tip: crear un prestamo con fecha de inicio pasada para probar prestamos vencidos.

## Archivos de texto (carpeta `data/`)
`empleados.txt`, `clientes.txt`, `prestamos.txt`, `pagos.txt` (registros, separados por `;`, se anexan al guardar en MySQL), `audit_log.txt` (acciones con marca de tiempo, visible en Reportes) y `report_*.txt` (reportes exportados). Ejemplos en `docs/sample-output/`.

## OOP, SOLID y patrones
- **Herencia:** `Employee` y `Client` extienden `Person`. **Encapsulamiento:** campos privados + getters. **Polimorfismo:** `describe()` / `toFileLine()` por subclase; cada menu extiende `Menu`.
- **S:** cada clase tiene un trabajo (DAO = SQL, Service = reglas, View = consola). **O:** nuevas formulas de interes implementan `InterestCalculator`. **D:** los servicios reciben sus dependencias en el constructor.
- **Patrones:** Singleton (`DBConnection`), DAO, Strategy (`InterestCalculator`), Template Method (`Menu`), Service layer.

## Simplificaciones conocidas (alcance academico)
El dinero usa `double` redondeado a 2 decimales (codigo de produccion deberia usar `BigDecimal`); solo interes simple; sin autenticacion.