package com.crediya.view;

import com.crediya.model.Employee;
import com.crediya.service.EmployeeService;
import com.crediya.util.InputHelper;
import java.util.List;

public class EmployeeMenu extends Menu {
    private final EmployeeService service;

    public EmployeeMenu(EmployeeService service) { this.service = service; }

    @Override protected String title() { return "EMPLEADOS"; }

    @Override protected List<String> options() { return List.of("Registrar empleado", "Listar empleados"); }

    @Override
    protected void execute(int option) {
        switch (option) {
            case 1 -> register();
            case 2 -> list();
        }
    }

    private void register() {
        String nombre = InputHelper.readString("Nombre completo: ");
        String documento = InputHelper.readString("Documento (solo digitos): ");
        String rol = InputHelper.readString("Rol: ");
        String correo = InputHelper.readString("Correo: ");
        double salario = InputHelper.readDouble("Salario: ", 0);
        Employee e = service.register(nombre, documento, rol, correo, salario);
        System.out.println("\nEmpleado registrado con ID " + e.getId() + ".");
    }

    private void list() {
        List<Employee> employees = service.listAll();
        if (employees.isEmpty()) { System.out.println("No hay empleados registrados."); return; }
        System.out.printf("%-4s %-24s %-12s %-20s %-30s %s%n", "ID", "Nombre", "Documento", "Rol", "Correo", "Salario");
        employees.forEach(System.out::println);
    }
}