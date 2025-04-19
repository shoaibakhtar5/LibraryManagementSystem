package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User authenticate(String username, String password, String role) throws SQLException {
        // No role check; all users can authenticate
        String query = "SELECT user_id, username, role, member_id, staff_id FROM Users WHERE username = ? AND password = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
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
        return null;
    }

    public void addUser(User user, String username, String password, String role, Integer memberId, Integer staffId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can add users");
        }
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

    public void updateUser(User user, int userId, String username, String password, String role, Integer memberId, Integer staffId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can update users");
        }
        String query = "UPDATE Users SET username = ?, password = ?, role = ?, member_id = ?, staff_id = ? WHERE user_id = ?";
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
            stmt.setInt(6, userId);
            stmt.executeUpdate();
        }
    }

    public void deleteUser(User user, int userId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can delete users");
        }
        String query = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}