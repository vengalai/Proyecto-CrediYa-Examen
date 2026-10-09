package com.crediya.service;

import com.crediya.dao.ClientDAO;
import com.crediya.dao.EmployeeDAO;
import com.crediya.dao.LoanDAO;
import com.crediya.exception.CrediYaException;
import com.crediya.model.Loan;
import com.crediya.model.LoanStatus;
import com.crediya.util.FileManager;
import com.crediya.util.MoneyUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LoanService {
    private final LoanDAO loanDAO;
    private final ClientDAO clientDAO;
    private final EmployeeDAO employeeDAO;
    private final InterestCalculator calculator;

    public LoanService(LoanDAO loanDAO, ClientDAO clientDAO, EmployeeDAO employeeDAO,
                       InterestCalculator calculator) {
        this.loanDAO = loanDAO;
        this.clientDAO = clientDAO;
        this.employeeDAO = employeeDAO;
        this.calculator = calculator;
    }

    /**
     * Validates the data and calculates interest, total and installment.
     * The loan is NOT saved yet, so the user can review it before confirming.
     */
    public Loan buildLoan(int clientId, int employeeId, double monto, double tasaMensual,
                          int cuotas, LocalDate fechaInicio) {
        try {
            if (clientDAO.findById(clientId).isEmpty())
                throw new CrediYaException("Client #" + clientId + " does not exist.");
            if (employeeDAO.findById(employeeId).isEmpty())
                throw new CrediYaException("Employee #" + employeeId + " does not exist.");
            boolean hasOverdue = loanDAO.findByClientId(clientId).stream()
                    .anyMatch(l -> l.computeStatus(LocalDate.now()) == LoanStatus.OVERDUE);
            if (hasOverdue)
                throw new CrediYaException("The client has overdue loans and cannot receive a new one.");
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while validating loan: " + ex.getMessage(), ex);
        }
        if (monto <= 0)                       throw new CrediYaException("Principal must be positive.");
        if (tasaMensual <= 0 || tasaMensual > 20) throw new CrediYaException("Monthly rate must be between 0 and 20%.");
        if (cuotas < 1 || cuotas > 120)   throw new CrediYaException("Term must be between 1 and 120 months.");

        double interest = calculator.calculateInterest(monto, tasaMensual, cuotas);
        double total = MoneyUtil.round2(monto + interest);
        double installment = MoneyUtil.round2(total / cuotas);
        return new Loan(0, clientId, employeeId, monto, tasaMensual, cuotas, fechaInicio, LoanStatus.ACTIVE);
    }

    public Loan save(Loan loan) {
        try {
            loan.setId(loanDAO.insert(loan));
            FileManager.append("prestamos.txt", loan.toFileLine());
            FileManager.log("Loan created: #" + loan.getId() + " client #" + loan.getClientId()
                    + " by employee #" + loan.getEmployeeId() + " total " + MoneyUtil.format(loan.getTotalAmount()));
            return loan;
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while saving loan: " + ex.getMessage(), ex);
        }
    }

    /** Recalculates the status of every loan and stores the ones that changed. Returns how many changed. */
    public int refreshStatuses() {
        try {
            int changed = 0;
            LocalDate today = LocalDate.now();
            for (Loan l : loanDAO.findAll()) {
                LoanStatus newStatus = l.computeStatus(today);
                if (newStatus != l.getStatus()) {
                    loanDAO.updateStatus(l.getId(), newStatus);
                    FileManager.log("Loan #" + l.getId() + " status: " + l.getStatus() + " -> " + newStatus);
                    changed++;
                }
            }
            return changed;
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while refreshing statuses: " + ex.getMessage(), ex);
        }
    }

    /** All loans with up-to-date statuses. */
    public List<Loan> findAll() {
        refreshStatuses();
        try {
            return loanDAO.findAll();
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while listing loans: " + ex.getMessage(), ex);
        }
    }

    public Loan findById(int id) {
        try {
            return loanDAO.findById(id).orElseThrow(() -> new CrediYaException("Loan #" + id + " not found."));
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while searching loan: " + ex.getMessage(), ex);
        }
    }
}