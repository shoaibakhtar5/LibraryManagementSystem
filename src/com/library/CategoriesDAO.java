package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriesDAO {
    public void addCategory(User user, String categoryName, String description) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can add categories");
        }
        String query = "INSERT INTO Categories (category_name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            stmt.setString(2, description);
            stmt.executeUpdate();
        }
    }

    public void updateCategory(User user, int categoryId, String categoryName, String description) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can update categories");
        }
        String query = "UPDATE Categories SET category_name = ?, description = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            stmt.setString(2, description);
            stmt.setInt(3, categoryId);
            stmt.executeUpdate();
        }
    }

    public void deleteCategory(User user, int categoryId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can delete categories");
        }
        String query = "DELETE FROM Categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
        }
    }

    public List<Category> getAllCategories(User user) throws SQLException {
        // All roles can view categories
        List<Category> categories = new ArrayList<>();
        String query = "SELECT category_id, category_name, description FROM Categories";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("description")
                ));
            }
        }
        return categories;
    }
}