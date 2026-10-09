package com.crediya.view;

import java.util.List;

public class MainMenu extends Menu {
    private final EmployeeMenu employeeMenu;
    private final ClientMenu clientMenu;
    private final LoanMenu loanMenu;
    private final PaymentMenu paymentMenu;
    private final ReportMenu reportMenu;

    public MainMenu(EmployeeMenu employeeMenu, ClientMenu clientMenu, LoanMenu loanMenu,
                    PaymentMenu paymentMenu, ReportMenu reportMenu) {
        this.employeeMenu = employeeMenu;
        this.clientMenu = clientMenu;
        this.loanMenu = loanMenu;
        this.paymentMenu = paymentMenu;
        this.reportMenu = reportMenu;
    }

    @Override protected String title() { return "CREDIYA - PRESTAMOS Y COBROS"; }

    @Override protected String backLabel() { return "Salir"; }

    @Override
    protected List<String> options() { return List.of("Empleados", "Clientes", "Prestamos", "Pagos", "Reportes"); }

    @Override
    protected void execute(int option) {
        switch (option) {
            case 1 -> employeeMenu.run();
            case 2 -> clientMenu.run();
            case 3 -> loanMenu.run();
            case 4 -> paymentMenu.run();
            case 5 -> reportMenu.run();
        }
    }
}