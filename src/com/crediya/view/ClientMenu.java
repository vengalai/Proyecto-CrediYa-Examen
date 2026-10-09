package com.crediya.view;

import com.crediya.model.Client;
import com.crediya.service.ClientService;
import com.crediya.util.InputHelper;
import java.util.List;

public class ClientMenu extends Menu {
    private final ClientService service;

    public ClientMenu(ClientService service) { this.service = service; }

    @Override protected String title() { return "CLIENTES"; }

    @Override
    protected List<String> options() {
        return List.of("Registrar cliente", "Listar clientes", "Ver cliente con prestamos");
    }

    @Override
    protected void execute(int option) {
        switch (option) {
            case 1 -> register();
            case 2 -> list();
            case 3 -> detail();
        }
    }

    private void register() {
        String nombre = InputHelper.readString("Nombre completo: ");
        String documento = InputHelper.readString("Documento (solo digitos): ");
        String telefono = InputHelper.readString("Telefono (solo digitos): ");
        String correo = InputHelper.readString("Correo: ");
        Client c = service.register(nombre, documento, telefono, correo);
        System.out.println("\nCliente registrado con ID " + c.getId() + ".");
    }

    private void list() {
        List<Client> clients = service.listAll();
        if (clients.isEmpty()) { System.out.println("No hay clientes registrados."); return; }
        System.out.printf("%-4s %-24s %-12s %-12s %-28s %-26s%n", "ID", "Nombre", "Documento", "Telefono", "Correo", "Prestamos");
        clients.forEach(System.out::println);
    }

    private void detail() {
        int id = InputHelper.readInt("ID del cliente: ", 1, Integer.MAX_VALUE);
        Client c = service.findById(id);
        System.out.println("\nNombre:    " + c.getName());
        System.out.println("Documento: " + c.getDocument());
        System.out.println("Telefono:  " + c.getPhone());
        System.out.println("Correo:    " + c.getEmail());
        System.out.println("Prestamos (" + c.getLoans().size() + "):");
        if (c.getLoans().isEmpty()) System.out.println("  (ninguno)");
        c.getLoans().forEach(l -> System.out.println("  " + l.describe()));
    }
}