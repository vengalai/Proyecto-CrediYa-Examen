package com.crediya.service;

import com.crediya.dao.EmployeeDAO;
import com.crediya.exception.CrediYaException;
import com.crediya.model.Employee;
import com.crediya.util.FileManager;
import com.crediya.util.Validator;
import java.sql.SQLException;
import java.util.List;

public class EmployeeService {
    private final EmployeeDAO dao;

    public EmployeeService(EmployeeDAO dao) { this.dao = dao; }

    public Employee register(String nombre, String documento, String rol, String correo, double salario) {
        if (!Validator.isValidDocument(documento))
            throw new CrediYaException("Document must contain 5 to 15 digits.");
        if (!Validator.isValidEmail(correo))
            throw new CrediYaException("Invalid email format.");
        try {
            if (dao.existsByDocument(documento))
                throw new CrediYaException("An employee with document " + documento + " already exists.");
            Employee e = new Employee(0, nombre, documento, rol, correo, salario);
            e.setId(dao.insert(e));
            FileManager.append("empleados.txt", e.toFileLine());
            FileManager.log("Employee registered: #" + e.getId() + " " + e.getName());
            return e;
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while registering employee: " + ex.getMessage(), ex);
        }
    }

    public List<Employee> listAll() {
        try {
            return dao.findAll();
        } catch (SQLException ex) {
            throw new CrediYaException("Database error while listing employees: " + ex.getMessage(), ex);
        }
    }
}