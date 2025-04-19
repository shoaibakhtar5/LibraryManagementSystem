package com.library;

import java.util.Date;

public class BookReview {
    private int reviewId;
    private int bookId;
    private int memberId;
    private int rating;
    private String comment;
    private Date reviewDate;

    public BookReview(int reviewId, int bookId, int memberId, int rating, String comment, Date reviewDate) {
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    // Getters
    public int getReviewId() { return reviewId; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Date getReviewDate() { return reviewDate; }
}