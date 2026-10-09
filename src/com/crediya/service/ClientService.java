package com.crediya.service;

import com.crediya.dao.ClientDAO;
import com.crediya.dao.LoanDAO;
import com.crediya.exception.CrediYaException;
import com.crediya.model.Client;
import com.crediya.model.Loan;
import com.crediya.util.FileManager;
import com.crediya.util.Validator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientService {
    private final ClientDAO clientDAO;
    private final LoanDAO loanDAO;

    public ClientService(ClientDAO clientDAO, LoanDAO loanDAO) {
        this.clientDAO = clientDAO;
        this.loanDAO = loanDAO;
    }

    public Client register(String nombre, String documento, String telefono, String correo) {
        if (!Validator.isValidDocument(documento))
            throw new CrediYaException("Document must contain 5 to 15 digits.");
        if (!Validator.isValidPhone(telefono))
            throw new CrediYaException("Phone must contain 7 to 15 digits.");
        if (!Validator.isValidEmail(correo))
            throw new CrediYaException("Invalid email format.");
        try {
            if (clientDAO.existsByDocument(documento))
                throw new CrediYaException("A client with document " + documento + " already exists.");
            Client c = new Client(0, nombre, documento, telefono, correo);
            c.setId(clientDAO.insert(c));
            FileManager.append("clientes.txt", c.toFileLine());
            FileManager.log("Client registered: #" + c.getId() + " " + c.getName());
            return c;
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while registering client: " + ex.getMessage(), ex);
        }
    }

    /** Returns all clients, each one with its loans attached (one query per table + groupingBy). */
    public List<Client> listAll() {
        try {
            List<Client> clients = clientDAO.findAll();
            Map<Integer, List<Loan>> loansByClient = loanDAO.findAll().stream()
                    .collect(Collectors.groupingBy(Loan::getClientId));
            clients.forEach(c -> c.setLoans(loansByClient.getOrDefault(c.getId(), new ArrayList<>())));
            return clients;
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while listing clients: " + ex.getMessage(), ex);
        }
    }

    public Client findById(int id) {
        return listAll().stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElseThrow(() -> new CrediYaException("Client #" + id + " not found."));
    }
}