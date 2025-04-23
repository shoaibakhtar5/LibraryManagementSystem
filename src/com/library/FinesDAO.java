package com.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinesDAO {

    private boolean memberExists(int memberId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private boolean transactionExists(Integer transactionId) throws SQLException {
        if (transactionId == null) {
            return true; // No transaction ID provided, so no need to validate
        }
        String query = "SELECT COUNT(*) FROM Transactions WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public void addFine(User user, int memberId, double amount, String reason, Integer transactionId) throws SQLException, SecurityException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can add fines");
        }

        // Validate memberId
        if (!memberExists(memberId)) {
            throw new SQLException("Member with ID " + memberId + " does not exist");
        }

        // Validate transactionId
        if (!transactionExists(transactionId)) {
            throw new SQLException("Transaction with ID " + transactionId + " does not exist");
        }

        String query = "INSERT INTO Fines (member_id, fine_amount, reason, fine_date, status, transaction_id) VALUES (?, ?, ?, CURDATE(), 'Pending', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            stmt.setDouble(2, amount);
            stmt.setString(3, reason);
            if (transactionId != null) {
                stmt.setInt(4, transactionId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    public List<Fine> getFinesByMember(int memberId) throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT f.*, m.name AS member_name, t.book_id, b.title " +
                "FROM Fines f " +
                "JOIN Members m ON f.member_id = m.member_id " +
                "LEFT JOIN Transactions t ON f.transaction_id = t.transaction_id " +
                "LEFT JOIN Books b ON t.book_id = b.book_id " +
                "WHERE f.member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                fines.add(new Fine(
                        rs.getInt("fine_id"),
                        rs.getInt("member_id"),
                        rs.getString("member_name"),
                        rs.getDouble("fine_amount"),
                        rs.getString("reason"),
                        rs.getDate("fine_date"),
                        rs.getString("status"),
                        rs.getObject("transaction_id") != null ? rs.getInt("transaction_id") : null,
                        rs.getObject("book_id") != null ? rs.getInt("book_id") : null,
                        rs.getString("title")
                ));
            }
        }
        return fines;
    }

    public List<Fine> getAllFines() throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT f.*, m.name AS member_name, t.book_id, b.title " +
                "FROM Fines f " +
                "JOIN Members m ON f.member_id = m.member_id " +
                "LEFT JOIN Transactions t ON f.transaction_id = t.transaction_id " +
                "LEFT JOIN Books b ON t.book_id = b.book_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                fines.add(new Fine(
                        rs.getInt("fine_id"),
                        rs.getInt("member_id"),
                        rs.getString("member_name"),
                        rs.getDouble("fine_amount"),
                        rs.getString("reason"),
                        rs.getDate("fine_date"),
                        rs.getString("status"),
                        rs.getObject("transaction_id") != null ? rs.getInt("transaction_id") : null,
                        rs.getObject("book_id") != null ? rs.getInt("book_id") : null,
                        rs.getString("title")
                ));
            }
        }
        return fines;
    }

    public void payFine(User user, int fineId) throws SQLException, SecurityException {
        String getFineQuery = "SELECT member_id, status FROM Fines WHERE fine_id = ?";
        int memberId;
        String status;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getFineQuery)) {
            stmt.setInt(1, fineId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                memberId = rs.getInt("member_id");
                status = rs.getString("status");
            } else {
                throw new SQLException("Fine not found");
            }
        }

        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            if (!user.hasRole("Member") || user.getMemberId() != memberId) {
                throw new SecurityException("You can only pay your own fines");
            }
        }

        if ("Paid".equalsIgnoreCase(status)) {
            throw new SQLException("Fine is already paid");
        }

        String updateQuery = "UPDATE Fines SET status = 'Paid' WHERE fine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, fineId);
            stmt.executeUpdate();
        }
    }
}