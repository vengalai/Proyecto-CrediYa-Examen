package com.crediya.model;

import com.crediya.util.MoneyUtil;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {
    private int id;
    private final int clientId;
    private final int employeeId;
    private final double principal;   // monto
    private final double monthlyRate; // interes (percentual mensual)
    private final int termMonths;     // cuotas
    private final LocalDate startDate;// fecha_inicio
    private LoanStatus status;

    private double totalInterest;    // calculado
    private double totalAmount;      // calculado
    private double monthlyInstallment; // calculado
    private double outstandingBalance; // saldo pendiente

    public Loan(int id, int clientId, int employeeId, double principal, double monthlyRate, int termMonths,
                LocalDate startDate, LoanStatus status) {
        this.id = id;
        this.clientId = clientId;
        this.employeeId = employeeId;
        this.principal = principal;
        this.monthlyRate = monthlyRate;
        this.termMonths = termMonths;
        this.startDate = startDate;
        this.status = status;
        this.outstandingBalance = principal;
        recalculate();
    }

    private void recalculate() {
        totalInterest = MoneyUtil.round2(principal * (monthlyRate / 100.0) * termMonths);
        totalAmount = MoneyUtil.round2(principal + totalInterest);
        monthlyInstallment = MoneyUtil.round2(totalAmount / termMonths);
    }

    // ---- getters / setters ----
    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }
    public int getClientId()              { return clientId; }
    public int getEmployeeId()            { return employeeId; }
    public double getPrincipal()          { return principal; }
    public double getMonthlyRate()        { return monthlyRate; }
    public int getTermMonths()            { return termMonths; }
    public double getTotalInterest()      { return totalInterest; }
    public double getTotalAmount()        { return totalAmount; }
    public double getMonthlyInstallment() { return monthlyInstallment; }
    public double getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(double b) { this.outstandingBalance = b; }
    public LocalDate getStartDate()       { return startDate; }
    public LoanStatus getStatus()         { return status; }
    public void setStatus(LoanStatus s)   { this.status = s; }

    // ---- business logic ----
    public double getTotalPaid() { return MoneyUtil.round2(totalAmount - outstandingBalance); }

    /** How many full installments the client has already covered with what he paid. */
    public int getInstallmentsCovered() {
        return (int) Math.floor((getTotalPaid() + 0.01) / monthlyInstallment);
    }

    /** Due date of the next unpaid installment (null when the loan is fully paid). */
    public LocalDate getNextDueDate() {
        if (outstandingBalance < 0.01) return null;
        int next = Math.min(getInstallmentsCovered() + 1, termMonths);
        return startDate.plusMonths(next);
    }

    public long getDaysOverdue(LocalDate today) {
        LocalDate due = getNextDueDate();
        return (due != null && due.isBefore(today)) ? ChronoUnit.DAYS.between(due, today) : 0;
    }

    /** Status rule: no balance -> PAID, next due date already passed -> OVERDUE, else ACTIVE. */
    public LoanStatus computeStatus(LocalDate today) {
        if (outstandingBalance < 0.01) return LoanStatus.PAID;
        return getDaysOverdue(today) > 0 ? LoanStatus.OVERDUE : LoanStatus.ACTIVE;
    }

    public String describe() {
        LocalDate due = getNextDueDate();
        return String.format("Loan #%-3d | principal %-15s | %5.2f%% x %2d m | installment %-14s | balance %-15s | next due %-10s | %s",
                id, MoneyUtil.format(principal), monthlyRate, termMonths,
                MoneyUtil.format(monthlyInstallment), MoneyUtil.format(outstandingBalance),
                due == null ? "-" : due.toString(), status);
    }

    public String toFileLine() {
        return String.join(";", String.valueOf(id), String.valueOf(clientId), String.valueOf(employeeId),
                String.valueOf(principal), String.valueOf(monthlyRate), String.valueOf(termMonths),
                String.valueOf(totalInterest), String.valueOf(totalAmount), String.valueOf(monthlyInstallment),
                startDate.toString());
    }

    @Override
    public String toString() { return describe(); }
}