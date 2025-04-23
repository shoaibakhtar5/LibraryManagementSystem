package com.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {
    public List<Member> getAllMembers(User user) throws SQLException, SecurityException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Unauthorized access");
        }
        List<Member> members = new ArrayList<>();
        String query = "SELECT * FROM Members";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
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

    public Member getMemberById(User user, int memberId) throws SQLException, SecurityException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff") && !(user.hasRole("Member") && user.getMemberId() == memberId)) {
            throw new SecurityException("Unauthorized access: You can only access your own member data");
        }
        String query = "SELECT * FROM Members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Member(
                            rs.getInt("member_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getDate("join_date"),
                            rs.getString("address")
                    );
                } else {
                    throw new SQLException("Member with ID " + memberId + " not found");
                }
            }
        }
    }

    public void addMember(User user, String name, String email, String phone, String address, String username, String password) throws SQLException, SecurityException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can add members");
        }
        String insertMemberQuery = "INSERT INTO Members (name, email, phone, join_date, address) VALUES (?, ?, ?, CURDATE(), ?)";
        int memberId;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertMemberQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                memberId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve member ID");
            }
        }

        // Create a corresponding User entry
        String insertUserQuery = "INSERT INTO Users (username, password, role, member_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUserQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, hash the password
            stmt.setString(3, "Member");
            stmt.setInt(4, memberId);
            stmt.executeUpdate();
        }
    }

    public void updateMember(User user, int memberId, String name, String email, String phone, String address) throws SQLException, SecurityException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Unauthorized access");
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

    public void deleteMember(User user, int memberId) throws SQLException, SecurityException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Unauthorized access");
        }
        String query = "DELETE FROM Members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            stmt.executeUpdate();
        }
    }
}