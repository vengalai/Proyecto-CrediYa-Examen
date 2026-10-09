package com.crediya.view;

import com.crediya.model.Loan;
import com.crediya.model.Payment;
import com.crediya.service.LoanService;
import com.crediya.service.PaymentService;
import com.crediya.util.InputHelper;
import com.crediya.util.MoneyUtil;
import java.time.LocalDate;
import java.util.List;

public class PaymentMenu extends Menu {
    private final PaymentService paymentService;
    private final LoanService loanService;

    public PaymentMenu(PaymentService paymentService, LoanService loanService) {
        this.paymentService = paymentService;
        this.loanService = loanService;
    }

    @Override protected String title() { return "PAGOS"; }

    @Override protected List<String> options() { return List.of("Registrar pago", "Historial de pagos de un prestamo"); }

    @Override
    protected void execute(int option) {
        switch (option) {
            case 1 -> register();
            case 2 -> history();
        }
    }

    private void register() {
        int loanId = InputHelper.readInt("ID del prestamo: ", 1, Integer.MAX_VALUE);
        Loan loan = loanService.findById(loanId);
        System.out.println(loan.describe());
        System.out.println("Monto sugerido (una cuota): " + MoneyUtil.format(
                Math.min(loan.getMonthlyInstallment(), loan.getOutstandingBalance())));

        double monto = InputHelper.readDouble("Monto a pagar: ", 0);
        LocalDate fechaPago = InputHelper.readDate("Fecha de pago", LocalDate.now());
        Payment p = paymentService.registerPayment(loanId, monto, fechaPago);

        System.out.println("\n------ RECIBO ------");
        System.out.println("ID de pago:  " + p.getId());
        System.out.println("ID prestamo:  " + p.getLoanId());
        System.out.println("Fecha:       " + p.getPaymentDate());
        System.out.println("Monto:       " + MoneyUtil.format(p.getAmount()));
        System.out.println("Nuevo saldo: " + MoneyUtil.format(p.getBalanceAfter()));
        System.out.println("Estado:      " + loanService.findById(loanId).getStatus());
    }

    private void history() {
        int loanId = InputHelper.readInt("ID del prestamo: ", 1, Integer.MAX_VALUE);
        List<Payment> payments = paymentService.historyByLoan(loanId);
        if (payments.isEmpty()) { System.out.println("Este prestamo no tiene pagos aun."); return; }
        payments.forEach(System.out::println);
        double total = payments.stream().mapToDouble(Payment::getAmount).sum();
        System.out.println("Total pagado: " + MoneyUtil.format(total) + " en " + payments.size() + " pagos.");
    }
}