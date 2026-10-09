package com.crediya.dao;

import com.crediya.model.Employee;
import com.crediya.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAO implements GenericDAO<Employee> {

    private Connection conn() throws SQLException { return DBConnection.getInstance().getConnection(); }

    @Override
    public int insert(Employee e) throws SQLException {
        String sql = "INSERT INTO empleados (nombre, documento, rol, correo, salario) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getDocument());
            ps.setString(3, e.getRole());
            ps.setString(4, e.getEmail());
            ps.setDouble(5, e.getSalary());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    @Override
    public List<Employee> findAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM empleados ORDER BY id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Optional<Employee> findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM empleados WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public boolean existsByDocument(String document) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT 1 FROM empleados WHERE documento = ?")) {
            ps.setString(1, document);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private Employee map(ResultSet rs) throws SQLException {
        return new Employee(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                rs.getString("rol"), rs.getString("correo"), rs.getDouble("salario"));
    }
}