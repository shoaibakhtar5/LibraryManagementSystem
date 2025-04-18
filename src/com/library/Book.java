package com.library;

public class Book {
    private int bookId;
    private String title;
    private String isbn;
    private String category;
    private String publisher;
    private int publicationYear;
    private int totalCopies;
    private int availableCopies;

    public Book(int bookId, String title, String isbn, String category, String publisher, int publicationYear, int totalCopies, int availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.isbn = isbn;
        this.category = category;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    // Getters
    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getIsbn() { return isbn; }
    public String getCategory() { return category; }
    public String getPublisher() { return publisher; }
    public int getPublicationYear() { return publicationYear; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
}