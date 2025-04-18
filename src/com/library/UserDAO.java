package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User authenticate(String username, String password, String role) throws SQLException {
        String query = "SELECT user_id, username, role, member_id, staff_id FROM Users WHERE username = ? AND password = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Plain text for demo; hashing later
            stmt.setString(3, role);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getObject("member_id") != null ? rs.getInt("member_id") : null,
                        rs.getObject("staff_id") != null ? rs.getInt("staff_id") : null
                );
            }
        }
        return null; // Authentication failed
    }

    public void addUser(String username, String password, String role, Integer memberId, Integer staffId) throws SQLException {
        String query = "INSERT INTO Users (username, password, role, member_id, staff_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            if (memberId != null) {
                stmt.setInt(4, memberId);
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            if (staffId != null) {
                stmt.setInt(5, staffId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }
}