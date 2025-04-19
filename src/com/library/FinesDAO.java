package com.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinesDAO {
    public List<Fine> getFines(User user) throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query;
        if (user.hasRole("Member")) {
            query = "SELECT f.*, b.title, m.name " +
                    "FROM Fines f " +
                    "JOIN Books b ON f.book_id = b.book_id " +
                    "JOIN Members m ON f.member_id = m.member_id " +
                    "WHERE f.member_id = ?";
        } else if (user.hasRole("Admin") || user.hasRole("Staff")) {
            query = "SELECT f.*, b.title, m.name " +
                    "FROM Fines f " +
                    "JOIN Books b ON f.book_id = b.book_id " +
                    "JOIN Members m ON f.member_id = m.member_id";
        } else {
            throw new SecurityException("Unauthorized access");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (user.hasRole("Member")) {
                stmt.setInt(1, user.getMemberId());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fines.add(new Fine(
                            rs.getInt("fine_id"),
                            rs.getInt("member_id"),
                            rs.getString("name"),
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getDouble("fine_amount"),
                            rs.getDate("fine_date"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return fines;
    }

    public void payFine(User user, int fineId) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Unauthorized access");
        }
        String query = "UPDATE Fines SET status = 'Paid' WHERE fine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, fineId);
            stmt.executeUpdate();
        }
    }
}