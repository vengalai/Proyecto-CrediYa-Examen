package com.crediya.model;

import com.crediya.util.MoneyUtil;
import java.time.LocalDate;

public class Payment {
    private int id;
    private final int loanId;
    private final double amount;
    private final LocalDate paymentDate;
    private final double balanceAfter;

    public Payment(int id, int loanId, double amount, LocalDate paymentDate, double balanceAfter) {
        this.id = id;
        this.loanId = loanId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.balanceAfter = balanceAfter;
    }

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }
    public int getLoanId()           { return loanId; }
    public double getAmount()        { return amount; }
    public LocalDate getPaymentDate(){ return paymentDate; }
    public double getBalanceAfter()  { return balanceAfter; }

    public String describe() {
        return String.format("Payment #%-4d | loan #%-3d | %s | paid %-15s | balance after %s",
                id, loanId, paymentDate, MoneyUtil.format(amount), MoneyUtil.format(balanceAfter));
    }

    public String toFileLine() {
        return String.join(";", String.valueOf(id), String.valueOf(loanId), String.valueOf(amount),
                paymentDate.toString(), String.valueOf(balanceAfter));
    }

    @Override
    public String toString() { return describe(); }
}