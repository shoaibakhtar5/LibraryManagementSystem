package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StaffDAO {
    public void addStaff(User user, String name, String email, String phone, String role) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can add staff");
        }
        String query = "INSERT INTO Staff (name, email, phone, hire_date, role) VALUES (?, ?, ?, CURDATE(), ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, role);
            stmt.executeUpdate();
        }
    }

    public void updateStaff(User user, int staffId, String name, String email, String phone, String role) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can update staff");
        }
        String query = "UPDATE Staff SET name = ?, email = ?, phone = ?, role = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, role);
            stmt.setInt(5, staffId);
            stmt.executeUpdate();
        }
    }

    public void deleteStaff(User user, int staffId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can delete staff");
        }
        String query = "DELETE FROM Staff WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, staffId);
            stmt.executeUpdate();
        }
    }

    public List<Staff> getAllStaff(User user) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can view all staff");
        }
        List<Staff> staff = new ArrayList<>();
        String query = "SELECT staff_id, name, email, phone, hire_date, role FROM Staff";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                staff.add(new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("hire_date"),
                        rs.getString("role")
                ));
            }
        }
        return staff;
    }
}