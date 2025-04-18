package com.library;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Test BookDAO
            BookDAO bookDAO = new BookDAO();
            // Add a book
            bookDAO.addBook("New Book", "9781112223334", 1, 1, 2023, 2);
            // List all books
            List<Book> books = bookDAO.getAllBooks();
            for (Book book : books) {
                System.out.printf("ID: %d, Title: %s, ISBN: %s, Category: %s, Publisher: %s, Year: %d, Copies: %d/%d%n",
                        book.getBookId(), book.getTitle(), book.getIsbn(), book.getCategory(), book.getPublisher(),
                        book.getPublicationYear(), book.getAvailableCopies(), book.getTotalCopies());
            }

            // Test MemberDAO
            MemberDAO memberDAO = new MemberDAO();
            // Add a member
            memberDAO.addMember("Test User", "test@example.com", "1231231234", "123 Test St");
            // List all members
            List<Member> members = memberDAO.getAllMembers();
            for (Member member : members) {
                System.out.printf("ID: %d, Name: %s, Email: %s, Phone: %s, Join Date: %s, Address: %s%n",
                        member.getMemberId(), member.getName(), member.getEmail(), member.getPhone(),
                        member.getJoinDate(), member.getAddress());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}