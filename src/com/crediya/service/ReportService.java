package com.crediya.service;

import com.crediya.model.Client;
import com.crediya.model.Loan;
import com.crediya.model.LoanStatus;
import com.crediya.util.MoneyUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** All reports use Lambda expressions and the Stream API. */
public class ReportService {
    private final LoanService loanService;
    private final ClientService clientService;

    public ReportService(LoanService loanService, ClientService clientService) {
        this.loanService = loanService;
        this.clientService = clientService;
    }

    /** Client + how many overdue loans + total overdue balance. */
    public record DelinquentClient(Client client, long overdueLoans, double overdueBalance) { }

    // ---------------- data methods ----------------

    /** Active loans ordered by the nearest due date. */
    public List<Loan> activeLoans() {
        return loanService.findAll().stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .sorted(Comparator.comparing(Loan::getNextDueDate))
                .collect(Collectors.toList());
    }

    /** Overdue loans, the most delayed first. */
    public List<Loan> overdueLoans() {
        LocalDate today = LocalDate.now();
        return loanService.findAll().stream()
                .filter(l -> l.getStatus() == LoanStatus.OVERDUE)
                .sorted(Comparator.comparingLong((Loan l) -> l.getDaysOverdue(today)).reversed())
                .collect(Collectors.toList());
    }

    /** Clients with at least one overdue loan, biggest debt first. */
    public List<DelinquentClient> delinquentClients() {
        Map<Integer, List<Loan>> overdueByClient = overdueLoans().stream()
                .collect(Collectors.groupingBy(Loan::getClientId));
        Map<Integer, Client> clientsById = clientService.listAll().stream()
                .collect(Collectors.toMap(Client::getId, c -> c));

        return overdueByClient.entrySet().stream()
                .map(e -> new DelinquentClient(
                        clientsById.get(e.getKey()),
                        e.getValue().size(),
                        e.getValue().stream().mapToDouble(Loan::getOutstandingBalance).sum()))
                .sorted(Comparator.comparingDouble(DelinquentClient::overdueBalance).reversed())
                .collect(Collectors.toList());
    }

    // ---------------- formatted reports (printed and exported to text files) ----------------

    public List<String> activeLoansReport() {
        List<String> lines = new ArrayList<>();
        lines.add("=== ACTIVE LOANS (" + LocalDate.now() + ") ===");
        List<Loan> loans = activeLoans();
        if (loans.isEmpty()) lines.add("No active loans.");
        loans.stream().map(Loan::describe).forEach(lines::add);
        lines.add("Total outstanding: "
                + MoneyUtil.format(loans.stream().mapToDouble(Loan::getOutstandingBalance).sum()));
        return lines;
    }

    public List<String> overdueLoansReport() {
        LocalDate today = LocalDate.now();
        List<String> lines = new ArrayList<>();
        lines.add("=== OVERDUE LOANS (" + today + ") ===");
        List<Loan> loans = overdueLoans();
        if (loans.isEmpty()) lines.add("No overdue loans.");
        loans.stream()
                .map(l -> l.describe() + " | " + l.getDaysOverdue(today) + " days late")
                .forEach(lines::add);
        lines.add("Total overdue balance: "
                + MoneyUtil.format(loans.stream().mapToDouble(Loan::getOutstandingBalance).sum()));
        return lines;
    }

    public List<String> delinquentClientsReport() {
        List<String> lines = new ArrayList<>();
        lines.add("=== DELINQUENT CLIENTS (" + LocalDate.now() + ") ===");
        List<DelinquentClient> list = delinquentClients();
        if (list.isEmpty()) lines.add("No delinquent clients.");
        list.stream()
                .map(d -> String.format("Client #%-3d %-24s doc %-12s phone %-12s | overdue loans: %d | owes %s",
                        d.client().getId(), d.client().getName(), d.client().getDocument(),
                        d.client().getPhone(), d.overdueLoans(), MoneyUtil.format(d.overdueBalance())))
                .forEach(lines::add);
        return lines;
    }

    /** Portfolio summary: loans per status and money totals. */
    public List<String> summaryReport() {
        List<Loan> all = loanService.findAll();
        Map<LoanStatus, Long> perStatus = all.stream()
                .collect(Collectors.groupingBy(Loan::getStatus, Collectors.counting()));
        List<String> lines = new ArrayList<>();
        lines.add("=== PORTFOLIO SUMMARY (" + LocalDate.now() + ") ===");
        for (LoanStatus s : LoanStatus.values())
            lines.add(String.format("%-8s loans: %d", s, perStatus.getOrDefault(s, 0L)));
        lines.add("Total lent (principal):  " + MoneyUtil.format(all.stream().mapToDouble(Loan::getPrincipal).sum()));
        lines.add("Total collected:         " + MoneyUtil.format(all.stream().mapToDouble(Loan::getTotalPaid).sum()));
        lines.add("Total outstanding:       " + MoneyUtil.format(all.stream().mapToDouble(Loan::getOutstandingBalance).sum()));
        return lines;
    }
}
