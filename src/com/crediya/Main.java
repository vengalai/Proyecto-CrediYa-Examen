package com.crediya;

import com.crediya.dao.ClientDAO;
import com.crediya.dao.EmployeeDAO;
import com.crediya.dao.LoanDAO;
import com.crediya.dao.PaymentDAO;
import com.crediya.exception.CrediYaException;
import com.crediya.service.ClientService;
import com.crediya.service.EmployeeService;
import com.crediya.service.LoanService;
import com.crediya.service.PaymentService;
import com.crediya.service.ReportService;
import com.crediya.service.SimpleInterestCalculator;
import com.crediya.util.DBConnection;
import com.crediya.util.FileManager;
import com.crediya.view.*;
import java.sql.SQLException;
/** Entry point: checks the database, builds every object (manual dependency injection) and starts the menu. */
public class Main {
    public static void main(String[] args) {
        System.out.println("CrediYa - connecting to MySQL...");
        try {
            DBConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.out.println("Could not connect to the database: " + e.getMessage());
            System.out.println("Check that MySQL is running, that sql/crediya_schema.sql was executed,");
            System.out.println("that config.properties is correct and that the MySQL JDBC driver is in lib/.");
            return;
        }

        // DAOs
        EmployeeDAO employeeDAO = new EmployeeDAO();
        ClientDAO clientDAO = new ClientDAO();
        LoanDAO loanDAO = new LoanDAO();
        PaymentDAO paymentDAO = new PaymentDAO();

        // Services
        EmployeeService employeeService = new EmployeeService(employeeDAO);
        ClientService clientService = new ClientService(clientDAO, loanDAO);
        LoanService loanService = new LoanService(loanDAO, clientDAO, employeeDAO, new SimpleInterestCalculator());
        PaymentService paymentService = new PaymentService(paymentDAO, loanDAO);
        ReportService reportService = new ReportService(loanService, clientService);

        try {
            int changed = loanService.refreshStatuses();
            System.out.println("Connected. Loan statuses updated: " + changed);
        } catch (CrediYaException e) {
            System.out.println("Warning: " + e.getMessage());
        }
        FileManager.log("Application started");

        // Views
        MainMenu menu = new MainMenu(
                new EmployeeMenu(employeeService),
                new ClientMenu(clientService),
                new LoanMenu(loanService, clientService, employeeService),
                new PaymentMenu(paymentService, loanService),
                new ReportMenu(reportService));
        menu.run();

        FileManager.log("Application closed");
        DBConnection.getInstance().close();
        System.out.println("Goodbye!");
    }
}