package com.library;

public class Author {
    private int authorId;
    private String name;
    private String bio;

    public Author(int authorId, String name, String bio) {
        this.authorId = authorId;
        this.name = name;
        this.bio = bio;
    }

    // Getters
    public int getAuthorId() { return authorId; }
    public String getName() { return name; }
    public String getBio() { return bio; }
}