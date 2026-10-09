package com.crediya.service;

import com.crediya.dao.LoanDAO;
import com.crediya.dao.PaymentDAO;
import com.crediya.exception.CrediYaException;
import com.crediya.model.Loan;
import com.crediya.model.LoanStatus;
import com.crediya.model.Payment;
import com.crediya.util.DBConnection;
import com.crediya.util.FileManager;
import com.crediya.util.MoneyUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PaymentService {
    private final PaymentDAO paymentDAO;
    private final LoanDAO loanDAO;

    public PaymentService(PaymentDAO paymentDAO, LoanDAO loanDAO) {
        this.paymentDAO = paymentDAO;
        this.loanDAO = loanDAO;
    }

    /**
     * Registers a payment. The payment INSERT and the loan UPDATE run in one
     * database transaction: both succeed or both are rolled back.
     */
    public Payment registerPayment(int loanId, double amount, LocalDate date) {
        Loan loan = findLoan(loanId);
        if (loan.getOutstandingBalance() < 0.01)
            throw new CrediYaException("Loan #" + loanId + " is already fully paid.");
        if (amount <= 0)
            throw new CrediYaException("The payment amount must be positive.");
        if (amount > loan.getOutstandingBalance() + 0.005)
            throw new CrediYaException("The payment exceeds the outstanding balance ("
                    + MoneyUtil.format(loan.getOutstandingBalance()) + ").");

        double newBalance = MoneyUtil.round2(loan.getOutstandingBalance() - amount);
        if (newBalance < 0.01) newBalance = 0;
        Payment payment = new Payment(0, loanId, amount, date, newBalance);

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);                       // start transaction

            payment.setId(paymentDAO.insert(payment));
            loan.setOutstandingBalance(newBalance);
            LoanStatus newStatus = loan.computeStatus(LocalDate.now());
            loan.setStatus(newStatus);
            loanDAO.updateBalanceAndStatus(loanId, newBalance, newStatus);

            conn.commit();                                   // both operations saved
        } catch (SQLException ex) {
            rollback(conn);
            throw new CrediYaException("Payment could not be saved: " + ex.getMessage(), ex);
        } finally {
            restoreAutoCommit(conn);
        }

        FileManager.append("payments.txt", payment.toFileLine());
        FileManager.log("Payment #" + payment.getId() + " of " + MoneyUtil.format(amount)
                + " on loan #" + loanId + ", new balance " + MoneyUtil.format(newBalance));
        return payment;
    }

    public List<Payment> historyByLoan(int loanId) {
        findLoan(loanId);   // makes sure the loan exists
        try {
            return paymentDAO.findByLoanId(loanId);
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while reading payments: " + ex.getMessage(), ex);
        }
    }

    private Loan findLoan(int loanId) {
        try {
            return loanDAO.findById(loanId).orElseThrow(() -> new CrediYaException("Loan #" + loanId + " not found."));
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while searching loan: " + ex.getMessage(), ex);
        }
    }

    private void rollback(Connection conn) {
        try { if (conn != null) conn.rollback(); } catch (SQLException ignored) { }
    }

    private void restoreAutoCommit(Connection conn) {
        try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) { }
    }
}
