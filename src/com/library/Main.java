package com.library;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Create test users
            User admin = new User(1, "admin", "Admin", null, null);
            User staff = new User(2, "staff", "Staff", null, 1);
            User member = new User(3, "member", "Member", 1, null);

            // Test MemberDAO
            MemberDAO memberDAO = new MemberDAO();
            try {
                memberDAO.addMember(admin, "New Member", "new@example.com", "1234567890", "123 Main St");
                System.out.println("Admin added member successfully");
            } catch (SQLException e) {
                System.out.println("Member add failed: " + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("Member add failed: " + e.getMessage());
            }
            try {
                memberDAO.addMember(member, "Invalid Member", "invalid@example.com", "0987654321", "456 Main St");
            } catch (SecurityException e) {
                System.out.println("Member member add failed: " + e.getMessage());
            }

            // Test UserDAO
            UserDAO userDAO = new UserDAO();
            try {
                userDAO.addUser(admin, "newuser", "password", "Staff", null, 1);
                System.out.println("Admin added user successfully");
            } catch (SecurityException e) {
                System.out.println("User add failed: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("User add failed: " + e.getMessage());
            }
            try {
                userDAO.addUser(staff, "invaliduser", "password", "Staff", null, 1);
            } catch (SecurityException e) {
                System.out.println("Staff user add failed: " + e.getMessage());
            }

            // Test TransactionDAO
            TransactionDAO transactionDAO = new TransactionDAO();
            try {
                transactionDAO.issueBook(staff, 1, 1, 1);
                System.out.println("Staff issued book successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Issue book failed: " + e.getMessage());
            }
            try {
                transactionDAO.deleteTransaction(admin, 2); // Adjust transaction_id if needed
                System.out.println("Admin deleted transaction successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Transaction delete failed: " + e.getMessage());
            }

            // Test CategoriesDAO
            CategoriesDAO categoriesDAO = new CategoriesDAO();
            try {
                categoriesDAO.addCategory(admin, "Science", "Scientific books");
                System.out.println("Admin added category successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Category add failed: " + e.getMessage());
            }
            List<Category> categories = categoriesDAO.getAllCategories(member);
            for (Category c : categories) {
                System.out.printf("Category ID: %d, Name: %s%n", c.getCategoryId(), c.getCategoryName());
            }

            // Test PublishersDAO
            PublishersDAO publishersDAO = new PublishersDAO();
            try {
                publishersDAO.addPublisher(admin, "New Pub", "newpub@example.com", "789 Pub St");
                System.out.println("Admin added publisher successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Publisher add failed: " + e.getMessage());
            }
            List<Publisher> publishers = publishersDAO.getAllPublishers(member);
            for (Publisher p : publishers) {
                System.out.printf("Publisher ID: %d, Name: %s%n", p.getPublisherId(), p.getPublisherName());
            }

            // Test AuthorsDAO
            AuthorsDAO authorsDAO = new AuthorsDAO();
            try {
                authorsDAO.addAuthor(admin, "New Author", "Author bio");
                System.out.println("Admin added author successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Author add failed: " + e.getMessage());
            }
            List<Author> authors = authorsDAO.getAllAuthors(member);
            for (Author a : authors) {
                System.out.printf("Author ID: %d, Name: %s%n", a.getAuthorId(), a.getName());
            }

            // Test StaffDAO
            StaffDAO staffDAO = new StaffDAO();
            try {
                staffDAO.addStaff(admin, "New Staff", "staff@example.com", "9876543210", "Clerk");
                System.out.println("Admin added staff successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Staff add failed: " + e.getMessage());
            }
            try {
                List<Staff> staffList = staffDAO.getAllStaff(staff);
                for (Staff s : staffList) {
                    System.out.printf("Staff ID: %d, Name: %s, Role: %s%n", s.getStaffId(), s.getName(), s.getRole());
                }
            } catch (SecurityException | SQLException e) {
                System.out.println("Staff fetch failed: " + e.getMessage());
            }

            // Test BookDAO
            BookDAO bookDAO = new BookDAO();
            try {
                bookDAO.addBook(admin, "New Book", "9780987654321", 1, 1, 2023, 5);
                System.out.println("Admin added book successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Book add failed: " + e.getMessage());
            }

            // Test BookReviewDAO
            BookReviewDAO reviewDAO = new BookReviewDAO();
            try {
                reviewDAO.addReview(member, 1, 1, 5, "Awesome book!");
                System.out.println("Member added review successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Review add failed: " + e.getMessage());
            }

            // Test ReservationsDAO
            ReservationDAO reservationsDAO = new ReservationDAO();
            try {
                reservationsDAO.addReservation(member, 1, 1);
                System.out.println("Member added reservation successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Reservation add failed: " + e.getMessage());
            }
            try {
                List<Reservation> reservation = reservationsDAO.getReservations(staff);
                for (Reservation r : reservation) {
                    System.out.printf("Reservation ID: %d, Book ID: %d, Status: %s%n",
                            r.getReservationId(), r.getBookId(), r.getStatus());
                }
            } catch (SecurityException | SQLException e) {
                System.out.println("Reservations fetch failed: " + e.getMessage());
            }

            // Test FinesDAO
            FinesDAO finesDAO = new FinesDAO();
            try {
                finesDAO.payFine(staff, 1);
                System.out.println("Staff added fine successfully");
            } catch (SecurityException | SQLException e) {
                System.out.println("Fine add failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}