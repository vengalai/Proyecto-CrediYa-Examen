package com.crediya.view;

import com.crediya.exception.CrediYaException;
import com.crediya.exception.DatoInvalidoException;
import com.crediya.model.Loan;
import com.crediya.model.Prestamo;
import com.crediya.service.ClientService;
import com.crediya.service.EmployeeService;
import com.crediya.service.GestorPrestamos;
import com.crediya.service.LoanService;
import com.crediya.util.InputHelper;
import com.crediya.util.MoneyUtil;
import java.time.LocalDate;
import java.util.List;

public class LoanMenu extends Menu {
    private final LoanService loanService;
    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final GestorPrestamos gestor;

    public LoanMenu(LoanService loanService, ClientService clientService, EmployeeService employeeService,
                    GestorPrestamos gestor) {
        this.loanService = loanService;
        this.clientService = clientService;
        this.employeeService = employeeService;
        this.gestor = gestor;
    }

    @Override protected String title() { return "PRESTAMOS"; }

    @Override
    protected List<String> options() {
        return List.of("Crear prestamo", "Listar prestamos", "Ver detalle de prestamo",
                "Listar con total a pagar (desde MySQL)", "Buscar prestamos por estado", "Demo de validaciones");
    }

    @Override
    protected void execute(int option) {
        switch (option) {
            case 1 -> create();
            case 2 -> list();
            case 3 -> detail();
            case 4 -> listWithTotal();
            case 5 -> searchByState();
            case 6 -> validationDemo();
        }
    }

    private void create() {
        System.out.println("\n-- Clientes --");
        clientService.listAll().forEach(c -> System.out.printf("  #%d %s%n", c.getId(), c.getName()));
        int clientId = InputHelper.readInt("ID del cliente: ", 1, Integer.MAX_VALUE);

        System.out.println("\n-- Empleados --");
        employeeService.listAll().forEach(e -> System.out.printf("  #%d %s (%s)%n", e.getId(), e.getName(), e.getRole()));
        int employeeId = InputHelper.readInt("ID del empleado (quien aprueba): ", 1, Integer.MAX_VALUE);

        double monto = InputHelper.readDouble("Monto a prestar: ", 0);
        double tasa = InputHelper.readDouble("Tasa de interes mensual (%), ej: 2.5: ", 0);
        int cuotas = InputHelper.readInt("Numero de cuotas (1-120): ", 1, 120);
        LocalDate fechaInicio = InputHelper.readDate("Fecha de inicio", LocalDate.now());

        Loan loan = loanService.buildLoan(clientId, employeeId, monto, tasa, cuotas, fechaInicio);
        System.out.println("\n--- Resumen del prestamo ---");
        printBreakdown(loan);
        if (InputHelper.confirm("\nGuardar este prestamo?")) {
            Prestamo p = new Prestamo(0, clientId, monto, tasa, cuotas, Prestamo.ACTIVO, employeeId, fechaInicio);
            gestor.registrarYGuardar(p);
            System.out.println("Prestamo guardado con ID " + p.getId() + ".");
        } else {
            System.out.println("Prestamo cancelado.");
        }
    }

    private void list() {
        List<Loan> loans = loanService.findAll();
        if (loans.isEmpty()) { System.out.println("No hay prestamos registrados."); return; }
        loans.forEach(System.out::println);
    }

    private void detail() {
        int id = InputHelper.readInt("ID del prestamo: ", 1, Integer.MAX_VALUE);
        loanService.refreshStatuses();
        Loan loan = loanService.findById(id);
        System.out.println("\nPrestamo #" + loan.getId() + "  (cliente #" + loan.getClientId()
                + ", empleado #" + loan.getEmployeeId() + ")");
        printBreakdown(loan);
    }


    private void listWithTotal() {
        int n = gestor.cargarDesdeBD();
        System.out.println("Prestamos cargados desde MySQL: " + n);
        gestor.listarPrestamos().forEach(System.out::println);
    }

    private void searchByState() {
        String estado = InputHelper.readString("Estado a buscar (Activo, Pagado o Mora): ");
        gestor.cargarDesdeBD();
        List<Prestamo> resultado = gestor.buscarPorEstado(estado);
        if (resultado.isEmpty()) {
            System.out.println("No hay prestamos con estado " + estado + ".");
            return;
        }
        resultado.forEach(System.out::println);
        System.out.println("Coincidencias: " + resultado.size());
    }


    private void validationDemo() {
        System.out.println("\nUsa un cliente SIN prestamos vencidos y un empleado existente.");
        int cliente = InputHelper.readInt("ID de un cliente existente: ", 1, Integer.MAX_VALUE);
        int empleado = InputHelper.readInt("ID de un empleado existente: ", 1, Integer.MAX_VALUE);
        System.out.println("\n-- Cada prestamo invalido debe ser rechazado --");
        probar("monto = 0",           demoPrestamo(cliente, empleado, 0, 2.5, 6),       DatoInvalidoException.class);
        probar("plazo = 0",           demoPrestamo(cliente, empleado, 500000, 2.5, 0),  DatoInvalidoException.class);
        probar("tasa negativa (-1)",  demoPrestamo(cliente, empleado, 500000, -1, 6),   DatoInvalidoException.class);
        probar("cliente inexistente", demoPrestamo(9999, empleado, 500000, 2.5, 6),     CrediYaException.class);
        probar("prestamo nulo",       null,                                             DatoInvalidoException.class);
    }

    private Prestamo demoPrestamo(int cliente, int empleado, double monto, double tasa, int plazo) {
        return new Prestamo(0, cliente, monto, tasa, plazo, Prestamo.ACTIVO, empleado, LocalDate.now());
    }


    private void probar(String caso, Prestamo p, Class<? extends CrediYaException> esperada) {
        try {
            gestor.registrarPrestamo(p);
            System.out.println("  [FALLO] " + caso + ": fue aceptado.");
        } catch (CrediYaException e) {
            String tipo = e.getClass().getSimpleName();
            if (e.getClass() == esperada)
                System.out.println("  [OK] " + caso + " -> " + tipo + ": " + e.getMessage());
            else
                System.out.println("  [AVISO] " + caso + " -> rechazado por otra razon (" + tipo + "): " + e.getMessage()
                        + "\n          Revisa que el cliente y el empleado existan y que el cliente no tenga prestamos vencidos.");
        }
    }

    private void printBreakdown(Loan l) {
        System.out.println("Monto:                " + MoneyUtil.format(l.getPrincipal()));
        System.out.println("Tasa mensual:         " + l.getMonthlyRate() + "%");
        System.out.println("Cuotas:               " + l.getTermMonths());
        System.out.println("Interes total:        " + MoneyUtil.format(l.getTotalInterest()));
        System.out.println("Total a pagar:        " + MoneyUtil.format(l.getTotalAmount()));
        System.out.println("Valor cuota mensual:  " + MoneyUtil.format(l.getMonthlyInstallment()));
        System.out.println("Fecha de inicio:      " + l.getStartDate());
        System.out.println("Total pagado:         " + MoneyUtil.format(l.getTotalPaid()));
        System.out.println("Saldo pendiente:      " + MoneyUtil.format(l.getOutstandingBalance()));
        System.out.println("Proxima fecha de pago:" + (l.getNextDueDate() == null ? "-" : l.getNextDueDate()));
        System.out.println("Estado:               " + l.getStatus());
    }
}