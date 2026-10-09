package com.crediya.view;

import com.crediya.model.Loan;
import com.crediya.service.ClientService;
import com.crediya.service.EmployeeService;
import com.crediya.service.LoanService;
import com.crediya.util.InputHelper;
import com.crediya.util.MoneyUtil;
import java.time.LocalDate;
import java.util.List;

public class LoanMenu extends Menu {
    private final LoanService loanService;
    private final ClientService clientService;
    private final EmployeeService employeeService;

    public LoanMenu(LoanService loanService, ClientService clientService, EmployeeService employeeService) {
        this.loanService = loanService;
        this.clientService = clientService;
        this.employeeService = employeeService;
    }

    @Override protected String title() { return "PRESTAMOS"; }

    @Override
    protected List<String> options() { return List.of("Crear prestamo", "Listar prestamos", "Ver detalle de prestamo"); }

    @Override
    protected void execute(int option) {
        switch (option) {
            case 1 -> create();
            case 2 -> list();
            case 3 -> detail();
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
            loanService.save(loan);
            System.out.println("Prestamo guardado con ID " + loan.getId() + ".");
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