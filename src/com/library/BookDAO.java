package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public void addBook(String title, String isbn, int categoryId, int publisherId, int publicationYear, int totalCopies) throws SQLException {
        String query = "INSERT INTO Books (title, isbn, category_id, publisher_id, publication_year, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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

    public List<Book> getAllBooks() throws SQLException {
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