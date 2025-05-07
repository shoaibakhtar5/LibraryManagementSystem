package com.library;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookReviewDAO {
    private static final String INSERT_REVIEW_SQL = """
        INSERT INTO Book_Reviews (book_id, member_id, rating, comment, review_date) 
        VALUES (?, ?, ?, ?, ?)""";

    private static final String UPDATE_REVIEW_SQL = """
        UPDATE Book_Reviews SET rating = ?, comment = ? 
        WHERE review_id = ?""";

    private static final String DELETE_REVIEW_SQL = "DELETE FROM Book_Reviews WHERE review_id = ?";

    private static final String SELECT_REVIEWS_BY_BOOK_SQL = """
        SELECT review_id, book_id, member_id, rating, comment, review_date 
        FROM Book_Reviews WHERE book_id = ?""";

    private static final String CHECK_REVIEW_OWNER_SQL = """
        SELECT member_id FROM Book_Reviews WHERE review_id = ?""";

    private static final String GET_AVERAGE_RATING_SQL = "CALL GetAverageBookRating(?)";

    public void addReview(User user, int memberId, int bookId, int rating, String comment) throws SQLException {
        validateReviewAccess(user, memberId, null);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_REVIEW_SQL)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            stmt.setInt(3, rating);
            stmt.setString(4, comment);
            stmt.setDate(5, Date.valueOf(LocalDate.now())); // Use current date for review_date
            stmt.executeUpdate();
        }
    }

    public void updateReview(User user, int reviewId, int rating, String comment) throws SQLException {
        validateReviewAccess(user, null, reviewId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_REVIEW_SQL)) {
            stmt.setInt(1, rating);
            stmt.setString(2, comment);
            stmt.setInt(3, reviewId);
            stmt.executeUpdate();
        }
    }

    public void deleteReview(User user, int reviewId) throws SQLException {
        validateReviewAccess(user, null, reviewId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_REVIEW_SQL)) {
            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
        }
    }

    public List<BookReview> getReviewsByBook(int bookId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_REVIEWS_BY_BOOK_SQL)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<BookReview> reviews = new ArrayList<>();
                while (rs.next()) {
                    reviews.add(new BookReview(
                            rs.getInt("review_id"),
                            rs.getInt("book_id"),
                            rs.getInt("member_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getDate("review_date").toLocalDate()
                    ));
                }
                return reviews;
            }
        }
    }

    public Optional<ReviewStats> getAverageRating(int bookId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_AVERAGE_RATING_SQL)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new ReviewStats(
                            rs.getString("title"),
                            rs.getDouble("average_rating"),
                            rs.getInt("review_count")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private void validateReviewAccess(User user, Integer memberId, Integer reviewId)
            throws SQLException {
        if (user.hasRole("Admin")) return;

        if (!user.hasRole("Member") || user.getMemberId() == null) {
            throw new SecurityException("Unauthorized access");
        }

        if (reviewId != null) {
            // For update/delete, check if user owns the review
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(CHECK_REVIEW_OWNER_SQL)) {
                stmt.setInt(1, reviewId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next() || rs.getInt("member_id") != user.getMemberId()) {
                        throw new SecurityException("You can only modify your own reviews");
                    }
                }
            }
        } else if (memberId != null && memberId != user.getMemberId()) {
            // For create, check if memberId matches user's memberId
            throw new SecurityException("You can only create reviews for yourself");
        }
    }
}

// Stats container
class ReviewStats {
    private final String title;
    private final double averageRating;
    private final int reviewCount;

    public ReviewStats(String title, double averageRating, int reviewCount) {
        this.title = title;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    // Getters
    public String getTitle() { return title; }
    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }

    @Override
    public String toString() {
        return String.format("%s - Average: %.1f (%d reviews)",
                title, averageRating, reviewCount);
    }
}