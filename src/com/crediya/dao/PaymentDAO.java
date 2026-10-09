package com.crediya.dao;

import com.crediya.model.Payment;
import com.crediya.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO implements GenericDAO<Payment> {

    private Connection conn() throws SQLException { return DBConnection.getInstance().getConnection(); }

    @Override
    public int insert(Payment p) throws SQLException {
        String sql = "INSERT INTO pagos (prestamo_id, monto, fecha_pago, balance_after) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getLoanId());
            ps.setDouble(2, p.getAmount());
            ps.setDate(3, java.sql.Date.valueOf(p.getPaymentDate()));
            ps.setDouble(4, p.getBalanceAfter());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM pagos ORDER BY fecha_pago, id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Optional<Payment> findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM pagos WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Payment> findByLoanId(int loanId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE prestamo_id = ? ORDER BY fecha_pago, id";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Payment map(ResultSet rs) throws SQLException {
        return new Payment(rs.getInt("id"), rs.getInt("prestamo_id"), rs.getDouble("monto"),
                rs.getDate("fecha_pago").toLocalDate(), rs.getDouble("balance_after"));
    }
}