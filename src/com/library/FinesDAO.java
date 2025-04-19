package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FinesDAO {
    public void addFine(User user, int transactionId, int memberId, double amount) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can add fines");
        }
        String query = "INSERT INTO Fines (transaction_id, member_id, amount, payment_status) VALUES (?, ?, ?, 'Pending')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, transactionId);
            stmt.setInt(2, memberId);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        }
    }

    public void updateFine(User user, int fineId, double amount, String paymentStatus) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can update fines");
        }
        String query = "UPDATE Fines SET amount = ?, payment_status = ?, payment_date = IF(payment_status = 'Paid', CURDATE(), NULL) WHERE fine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, paymentStatus);
            stmt.setInt(3, fineId);
            stmt.executeUpdate();
        }
    }

    public void deleteFine(User user, int fineId) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can delete fines");
        }
        String query = "DELETE FROM Fines WHERE fine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, fineId);
            stmt.executeUpdate();
        }
    }

    public List<Fine> getAllFines(User user) throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query;
        if (user.hasRole("Admin") || user.hasRole("Staff")) {
            query = "SELECT fine_id, transaction_id, member_id, amount, payment_status, payment_date FROM Fines";
        } else if (user.hasRole("Member") && user.getMemberId() != null) {
            query = "SELECT fine_id, transaction_id, member_id, amount, payment_status, payment_date FROM Fines WHERE member_id = ?";
        } else {
            throw new SecurityException("Unauthorized to view fines");
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (user.hasRole("Member") && user.getMemberId() != null) {
                stmt.setInt(1, user.getMemberId());
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                fines.add(new Fine(
                        rs.getInt("fine_id"),
                        rs.getInt("transaction_id"),
                        rs.getInt("member_id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_status"),
                        rs.getDate("payment_date")
                ));
            }
        }
        return fines;
    }
}