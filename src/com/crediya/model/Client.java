package com.crediya.model;

import java.util.ArrayList;
import java.util.List;

public class Client extends Person {
    private final String phone;
    private final String email;
    private List<Loan> loans = new ArrayList<>();   // associated loans (1 client -> N loans)

    public Client(int id, String nombre, String document, String phone, String email) {
        super(id, nombre, document, email);
        this.phone = phone;
        this.email = email;
    }

    public String getPhone()        { return phone; }
    public String getEmail()        { return email; }
    public List<Loan> getLoans()    { return loans; }
    public void setLoans(List<Loan> loans) { this.loans = loans; }

    @Override public String getType() { return "CLIENT"; }

    @Override
    public String describe() {
        return String.format("%-4d %-24s %-12s %-12s %-28s %-26s loans: %d",
                getId(), getName(), getDocument(), phone, email, loans.size());
    }

    @Override
    public String toFileLine() {
        return String.join(";", String.valueOf(getId()), getName(), getDocument(), phone, email);
    }
}