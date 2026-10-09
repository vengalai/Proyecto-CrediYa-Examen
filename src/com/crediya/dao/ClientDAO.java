package com.crediya.dao;

import com.crediya.model.Client;
import com.crediya.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAO implements GenericDAO<Client> {

    private Connection conn() throws SQLException { return DBConnection.getInstance().getConnection(); }

    @Override
    public int insert(Client c) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, documento, correo, telefono) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDocument());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM clientes ORDER BY id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Optional<Client> findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public boolean existsByDocument(String document) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT 1 FROM clientes WHERE documento = ?")) {
            ps.setString(1, document);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                rs.getString("telefono"), rs.getString("correo"));
    }
}