package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PublishersDAO {
    public void addPublisher(User user, String publisherName, String contactEmail, String address) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can add publishers");
        }
        String query = "INSERT INTO Publishers (publisher_name, contact_email, address) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, publisherName);
            stmt.setString(2, contactEmail);
            stmt.setString(3, address);
            stmt.executeUpdate();
        }
    }

    public void updatePublisher(User user, int publisherId, String publisherName, String contactEmail, String address) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can update publishers");
        }
        String query = "UPDATE Publishers SET publisher_name = ?, contact_email = ?, address = ? WHERE publisher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, publisherName);
            stmt.setString(2, contactEmail);
            stmt.setString(3, address);
            stmt.setInt(4, publisherId);
            stmt.executeUpdate();
        }
    }

    public void deletePublisher(User user, int publisherId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can delete publishers");
        }
        String query = "DELETE FROM Publishers WHERE publisher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, publisherId);
            stmt.executeUpdate();
        }
    }

    public List<Publisher> getAllPublishers(User user) throws SQLException {
        // All roles can view publishers
        List<Publisher> publishers = new ArrayList<>();
        String query = "SELECT publisher_id, publisher_name, contact_email, address FROM Publishers";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                publishers.add(new Publisher(
                        rs.getInt("publisher_id"),
                        rs.getString("publisher_name"),
                        rs.getString("contact_email"),
                        rs.getString("address")
                ));
            }
        }
        return publishers;
    }
}