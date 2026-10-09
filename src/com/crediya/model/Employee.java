package com.crediya.model;

import com.crediya.util.MoneyUtil;

public class Employee extends Person {
    private final String role;
    private final double salary;

    public Employee(int id, String nombre, String document, String role, String email, double salary) {
        super(id, nombre, document, email);
        this.role = role;
        this.salary = salary;
    }

    public String getRole()   { return role; }
    public double getSalary() { return salary; }

    @Override public String getType() { return "EMPLOYEE"; }

    @Override
    public String describe() {
        return String.format("%-4d %-24s %-12s %-20s %-30s %s",
                getId(), getName(), getDocument(), role, getEmail(), MoneyUtil.format(salary));
    }

    @Override
    public String toFileLine() {
        return String.join(";", String.valueOf(getId()), getName(), getDocument(), role, getEmail(),
                String.valueOf(salary));
    }
}