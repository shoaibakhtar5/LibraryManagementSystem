package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MemberDAO {
    public void addMember(User user, String name, String email, String phone, String address) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can add members");
        }
        // Check for duplicate email
        String checkQuery = "SELECT COUNT(*) FROM Members WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Email already exists: " + email);
            }
        }

        String query = "INSERT INTO Members (name, email, phone, join_date, address) VALUES (?, ?, ?, CURDATE(), ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.executeUpdate();
        }
    }

    public void updateMember(User user, int memberId, String name, String email, String phone, String address) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can update members");
        }
        // Check for duplicate email (excluding the current member)
        String checkQuery = "SELECT COUNT(*) FROM Members WHERE email = ? AND member_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, email);
            checkStmt.setInt(2, memberId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Email already exists: " + email);
            }
        }

        String query = "UPDATE Members SET name = ?, email = ?, phone = ?, address = ? WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setInt(5, memberId);
            stmt.executeUpdate();
        }
    }

    public void deleteMember(User user, int memberId) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can delete members");
        }
        String query = "DELETE FROM Members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            stmt.executeUpdate();
        }
    }

    public List<Member> getAllMembers(User user) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can view all members");
        }
        List<Member> members = new ArrayList<>();
        String query = "SELECT member_id, name, email, phone, join_date, address FROM Members";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                members.add(new Member(
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("join_date"),
                        rs.getString("address")
                ));
            }
        }
        return members;
    }
}