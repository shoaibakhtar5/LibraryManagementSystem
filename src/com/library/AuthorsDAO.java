package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthorsDAO {
    public void addAuthor(User user, String name, String bio) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can add authors");
        }
        String query = "INSERT INTO Authors (name, bio) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, bio);
            stmt.executeUpdate();
        }
    }

    public void updateAuthor(User user, int authorId, String name, String bio) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can update authors");
        }
        String query = "UPDATE Authors SET name = ?, bio = ? WHERE author_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, bio);
            stmt.setInt(3, authorId);
            stmt.executeUpdate();
        }
    }

    public void deleteAuthor(User user, int authorId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can delete authors");
        }
        String query = "DELETE FROM Authors WHERE author_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, authorId);
            stmt.executeUpdate();
        }
    }

    public void linkAuthorToBook(User user, int bookId, int authorId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can link authors to books");
        }
        String checkQuery = "SELECT COUNT(*) FROM Book_Authors WHERE book_id = ? AND author_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, bookId);
            checkStmt.setInt(2, authorId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Link already exists
            }
        }

        String query = "INSERT INTO Book_Authors (book_id, author_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, authorId);
            stmt.executeUpdate();
        }
    }

    public List<Author> getAllAuthors(User user) throws SQLException {
        // All roles can view authors
        List<Author> authors = new ArrayList<>();
        String query = "SELECT author_id, name, bio FROM Authors";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                authors.add(new Author(
                        rs.getInt("author_id"),
                        rs.getString("name"),
                        rs.getString("bio")
                ));
            }
        }
        return authors;
    }
}