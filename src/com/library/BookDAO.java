package com.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO {
    private static final String INSERT_BOOK_SQL = """
        INSERT INTO Books (title, isbn, category_id, publisher_id, 
        publication_year, total_copies, available_copies) 
        VALUES (?, ?, ?, ?, ?, ?, ?)""";

    private static final String UPDATE_BOOK_SQL = """
        UPDATE Books SET title = ?, isbn = ?, category_id = ?, 
        publisher_id = ?, publication_year = ?, total_copies = ?, 
        available_copies = ? WHERE book_id = ?""";

    private static final String DELETE_BOOK_SQL = "DELETE FROM Books WHERE book_id = ?";

    private static final String SELECT_ALL_BOOKS_SQL = """
        SELECT b.book_id, b.title, b.isbn, c.category_name, p.publisher_name, 
        b.publication_year, b.total_copies, b.available_copies 
        FROM Books b JOIN Categories c ON b.category_id = c.category_id 
        JOIN Publishers p ON b.publisher_id = p.publisher_id""";

    private static final String CHECK_ISBN_SQL = "SELECT COUNT(*) FROM Books WHERE isbn = ?";

    public void addBook(User user, String title, String isbn, int categoryId, int publisherId,
                        int publicationYear, int totalCopies) throws SQLException {
        validateAdminAccess(user);
        validateIsbnUniqueness(isbn);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_BOOK_SQL)) {
            stmt.setString(1, title);
            stmt.setString(2, isbn);
            stmt.setInt(3, categoryId);
            stmt.setInt(4, publisherId);
            stmt.setInt(5, publicationYear);
            stmt.setInt(6, totalCopies);
            stmt.setInt(7, totalCopies); // Available copies same as total initially
            stmt.executeUpdate();
        }
    }

    public void updateBook(User user, int bookId, String title, String isbn, int categoryId,
                           int publisherId, int publicationYear, int totalCopies) throws SQLException {
        validateAdminAccess(user);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_BOOK_SQL)) {
            stmt.setString(1, title);
            stmt.setString(2, isbn);
            stmt.setInt(3, categoryId);
            stmt.setInt(4, publisherId);
            stmt.setInt(5, publicationYear);
            stmt.setInt(6, totalCopies);
            stmt.setInt(7, totalCopies); // Available copies same as total initially
            stmt.setInt(8, bookId);
            stmt.executeUpdate();
        }
    }

    public void deleteBook(User user, int bookId) throws SQLException {
        validateAdminAccess(user);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BOOK_SQL)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        }
    }

    public List<Book> getAllBooks(User user) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_BOOKS_SQL);
             ResultSet rs = stmt.executeQuery()) {

            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            return books;
        }
    }

    public Optional<Book> getBookById(User user, int bookId) throws SQLException {
        String sql = SELECT_ALL_BOOKS_SQL + " WHERE b.book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSetToBook(rs)) : Optional.empty();
            }
        }
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("isbn"),
                rs.getString("category_name"),
                rs.getString("publisher_name"),
                rs.getInt("publication_year"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        );
    }

    private void validateAdminAccess(User user) {
        if (!user.hasRole("Admin")) {
            throw new SecurityException("Only Admin can perform this action");
        }
    }

    private void validateIsbnUniqueness(String isbn) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CHECK_ISBN_SQL)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("ISBN already exists");
                }
            }
        }
    }
}