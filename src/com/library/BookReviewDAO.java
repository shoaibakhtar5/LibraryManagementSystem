package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookReviewDAO {
    public void addReview(User user, int bookId, int memberId, int rating, String comment) throws SQLException {
        if (!user.hasRole("Admin") && (!user.hasRole("Member") || user.getMemberId() == null || user.getMemberId() != memberId)) {
            throw new SecurityException("Only Admin or the Member who owns the review can add reviews");
        }
        String query = "INSERT INTO Book_Reviews (book_id, member_id, rating, comment, review_date) VALUES (?, ?, ?, ?, CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            stmt.setInt(3, rating);
            stmt.setString(4, comment);
            stmt.executeUpdate();
        }
    }

    public void updateReview(User user, int reviewId, int rating, String comment) throws SQLException {
        if (!user.hasRole("Admin")) {
            String checkQuery = "SELECT member_id FROM Book_Reviews WHERE review_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, reviewId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next() || user.getMemberId() == null || rs.getInt("member_id") != user.getMemberId()) {
                    throw new SecurityException("Only Admin or the Member who owns the review can update reviews");
                }
            }
        }
        String query = "UPDATE Book_Reviews SET rating = ?, comment = ? WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rating);
            stmt.setString(2, comment);
            stmt.setInt(3, reviewId);
            stmt.executeUpdate();
        }
    }

    public void deleteReview(User user, int reviewId) throws SQLException {
        if (!user.hasRole("Admin")) {
            String checkQuery = "SELECT member_id FROM Book_Reviews WHERE review_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, reviewId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next() || user.getMemberId() == null || rs.getInt("member_id") != user.getMemberId()) {
                    throw new SecurityException("Only Admin or the Member who owns the review can delete reviews");
                }
            }
        }
        String query = "DELETE FROM Book_Reviews WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
        }
    }

    public List<BookReview> getReviewsByBook(User user, int bookId) throws SQLException {
        // All roles can view reviews
        List<BookReview> reviews = new ArrayList<>();
        String query = "SELECT review_id, book_id, member_id, rating, comment, review_date FROM Book_Reviews WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reviews.add(new BookReview(
                        rs.getInt("review_id"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getDate("review_date")
                ));
            }
        }
        return reviews;
    }

    public String getAverageRating(User user, int bookId) throws SQLException {
        // All roles can view average rating
        String query = "CALL GetAverageBookRating(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return String.format("Book: %s, Average Rating: %.2f, Reviews: %d",
                        rs.getString("title"), rs.getDouble("average_rating"), rs.getInt("review_count"));
            }
        }
        return "No reviews found";
    }
}