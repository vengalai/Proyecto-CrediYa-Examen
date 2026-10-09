package com.crediya.dao;

import com.crediya.model.Loan;
import com.crediya.model.LoanStatus;
import com.crediya.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAO implements GenericDAO<Loan> {

    private Connection conn() throws SQLException { return DBConnection.getInstance().getConnection(); }

    @Override
    public int insert(Loan l) throws SQLException {
        String sql = "INSERT INTO prestamos (cliente_id, empleado_id, monto, interes, cuotas, "
                + "total_interest, total_amount, monthly_installment, outstanding_balance, fecha_inicio, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, l.getClientId());
            ps.setInt(2, l.getEmployeeId());
            ps.setDouble(3, l.getPrincipal());
            ps.setDouble(4, l.getMonthlyRate());
            ps.setInt(5, l.getTermMonths());
            ps.setDouble(6, l.getTotalInterest());
            ps.setDouble(7, l.getTotalAmount());
            ps.setDouble(8, l.getMonthlyInstallment());
            ps.setDouble(9, l.getOutstandingBalance());
            ps.setDate(10, Date.valueOf(l.getStartDate()));
            ps.setString(11, l.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    @Override
    public List<Loan> findAll() throws SQLException {
        List<Loan> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM prestamos ORDER BY id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Optional<Loan> findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM prestamos WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Loan> findByClientId(int clientId) throws SQLException {
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM prestamos WHERE cliente_id = ? ORDER BY id")) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Used after a payment: updates the balance and the status together. */
    public void updateBalanceAndStatus(int loanId, double balance, LoanStatus status) throws SQLException {
        String sql = "UPDATE prestamos SET outstanding_balance = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, balance);
            ps.setString(2, status.name());
            ps.setInt(3, loanId);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int loanId, LoanStatus status) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE prestamos SET estado = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, loanId);
            ps.executeUpdate();
        }
    }

    private Loan map(ResultSet rs) throws SQLException {
        return new Loan(rs.getInt("id"), rs.getInt("cliente_id"), rs.getInt("empleado_id"),
                rs.getDouble("monto"), rs.getDouble("interes"), rs.getInt("cuotas"),
                rs.getDate("fecha_inicio").toLocalDate(), LoanStatus.valueOf(rs.getString("estado")));
    }
}