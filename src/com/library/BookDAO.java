package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public void addBook(User user, String title, String isbn, int categoryId, String publisherName, int publicationYear, int totalCopies) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can add books");
        }
        String checkQuery = "SELECT COUNT(*) FROM Books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, isbn);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("ISBN already exists");
            }
        }

        Connection conn = null;
        int publisherId = getOrCreatePublisherId(String.valueOf(publisherName), conn);

        String query = "INSERT INTO Books (title, isbn, category_id, publisher_id, publication_year, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, isbn);
            stmt.setInt(3, categoryId);
            stmt.setInt(4, publisherId);
            stmt.setInt(5, publicationYear);
            stmt.setInt(6, totalCopies);
            stmt.setInt(7, totalCopies);
            stmt.executeUpdate();
        }
    }

    private int getOrCreatePublisherId(String publisherName, Connection conn) throws SQLException {
        String checkQuery = "SELECT publisher_id FROM Publishers WHERE publisher_name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, publisherName);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("publisher_id");
            }
        }

        String insertQuery = "INSERT INTO Publishers (publisher_name) VALUES (?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, publisherName);
            insertStmt.executeUpdate();
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        throw new SQLException("Failed to create new publisher");
    }

    public void updateBook(User user, int bookId, String title, String isbn, int categoryId, int publisherId, int publicationYear, int totalCopies) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can update books");
        }
        String query = "UPDATE Books SET title = ?, isbn = ?, category_id = ?, publisher_id = ?, publication_year = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, isbn);
            stmt.setInt(3, categoryId);
            stmt.setInt(4, publisherId);
            stmt.setInt(5, publicationYear);
            stmt.setInt(6, totalCopies);
            stmt.setInt(7, totalCopies);
            stmt.setInt(8, bookId);
            stmt.executeUpdate();
        }
    }

    public void deleteBook(User user, int bookId) throws SQLException {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can delete books");
        }
        String query = "DELETE FROM Books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        }
    }

    public List<Book> getAllBooks(User user) throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT b.book_id, b.title, b.isbn, c.category_name, p.publisher_name, b.publication_year, b.total_copies, b.available_copies " +
                "FROM Books b JOIN Categories c ON b.category_id = c.category_id JOIN Publishers p ON b.publisher_id = p.publisher_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("isbn"),
                        rs.getString("category_name"),
                        rs.getString("publisher_name"),
                        rs.getInt("publication_year"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                ));
            }
        }
        return books;
    }
}