package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionDAO {
    public void issueBook(int bookId, int memberId, int staffId) throws SQLException {
        // Check book availability
        String checkQuery = "SELECT available_copies FROM Books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next() || rs.getInt("available_copies") <= 0) {
                throw new SQLException("Book not available");
            }
        }

        // Insert transaction
        String insertQuery = "INSERT INTO Transactions (book_id, member_id, staff_id, issue_date, due_date) VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setInt(1, bookId);
            insertStmt.setInt(2, memberId);
            insertStmt.setInt(3, staffId);
            insertStmt.executeUpdate();
        }

        // Update book availability
        String updateQuery = "UPDATE Books SET available_copies = available_copies - 1 WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, bookId);
            updateStmt.executeUpdate();
        }
    }

    public void returnBook(int transactionId) throws SQLException {
        // Check if already returned
        String checkQuery = "SELECT return_date FROM Transactions WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, transactionId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next() || rs.getDate("return_date") != null) {
                throw new SQLException("Invalid transaction or book already returned");
            }
        }

        // Update transaction (trigger calculates fine)
        String updateTransQuery = "UPDATE Transactions SET return_date = CURDATE() WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateTransQuery)) {
            updateStmt.setInt(1, transactionId);
            updateStmt.executeUpdate();
        }

        // Update book availability
        String updateBookQuery = "UPDATE Books SET available_copies = available_copies + 1 WHERE book_id = (SELECT book_id FROM Transactions WHERE transaction_id = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateBookQuery)) {
            updateStmt.setInt(1, transactionId);
            updateStmt.executeUpdate();
        }
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT transaction_id, book_id, member_id, staff_id, issue_date, due_date, return_date, fine FROM Transactions";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id"),
                        rs.getInt("staff_id"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        rs.getDouble("fine")
                ));
            }
        }
        return transactions;
    }
}