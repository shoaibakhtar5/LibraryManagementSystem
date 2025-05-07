package com.library;

import java.util.Objects;

public class Book {
    private final int bookId;
    private final String title;
    private final String isbn;
    private final String categoryName;
    private final String publisherName;
    private final int publicationYear;
    private final int totalCopies;
    private final int availableCopies;

    public Book(int bookId, String title, String isbn, String categoryName,
                String publisherName, int publicationYear, int totalCopies,
                int availableCopies) {
        this.bookId = bookId;
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.isbn = validateIsbn(isbn);
        this.categoryName = Objects.requireNonNull(categoryName);
        this.publisherName = Objects.requireNonNull(publisherName);
        this.publicationYear = validateYear(publicationYear);
        this.totalCopies = validateCopies(totalCopies);
        this.availableCopies = validateAvailableCopies(availableCopies, totalCopies);
    }

    private String validateIsbn(String isbn) {
        if (isbn == null || !isbn.matches("^\\d{10}(\\d{3})?$")) {
            throw new IllegalArgumentException("Invalid ISBN format");
        }
        return isbn;
    }

    private int validateYear(int year) {
        if (year < 1800 || year > java.time.Year.now().getValue()) {
            throw new IllegalArgumentException("Invalid publication year");
        }
        return year;
    }

    private int validateCopies(int copies) {
        if (copies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative");
        }
        return copies;
    }

    private int validateAvailableCopies(int available, int total) {
        if (available < 0 || available > total) {
            throw new IllegalArgumentException("Invalid available copies");
        }
        return available;
    }

    // Getters
    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getIsbn() { return isbn; }
    public String getCategoryName() { return categoryName; }
    public String getPublisherName() { return publisherName; }
    public int getPublicationYear() { return publicationYear; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    @Override
    public String toString() {
        return title + " (" + publicationYear + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return bookId == book.bookId && Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, isbn);
    }
}