package com.library;

import java.time.LocalDate;
import java.util.Objects;

public class BookReview {
    private final int reviewId;
    private final int bookId;
    private final int memberId;
    private final int rating;
    private final String comment;
    private final LocalDate reviewDate;

    public BookReview(int reviewId, int bookId, int memberId, int rating,
                      String comment, LocalDate reviewDate) {
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.rating = validateRating(rating);
        this.comment = Objects.requireNonNull(comment, "Comment cannot be null");
        this.reviewDate = Objects.requireNonNull(reviewDate, "Review date cannot be null");
    }

    private int validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return rating;
    }

    // Getters
    public int getReviewId() { return reviewId; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDate getReviewDate() { return reviewDate; }

    @Override
    public String toString() {
        return String.format("%d stars - %s", rating, comment);
    }
}