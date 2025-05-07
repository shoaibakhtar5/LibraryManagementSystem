package com.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    // Modified: Accepts connection to avoid locking across different connections
    private void updateBookCopies(Connection conn, int bookId, int change) throws SQLException {
        String query = "UPDATE Books SET available_copies = LEAST(available_copies + ?, total_copies) WHERE book_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setQueryTimeout(60); // Increased to 60 seconds
            stmt.setInt(1, change);
            stmt.setInt(2, bookId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Book with ID " + bookId + " not found");
            }
        }
    }

    private boolean hasActiveReservation(User user, int memberId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Reservations WHERE member_id = ? AND status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean hasAvailableCopies(int bookId) throws SQLException {
        String query = "SELECT available_copies FROM Books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available_copies") > 0;
                }
            }
        }
        throw new SQLException("Book with ID " + bookId + " not found");
    }

    public List<Reservation> getReservations(User user) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query;
        if (user.hasRole("Member")) {
            query = "SELECT r.*, b.title, m.name FROM Reservations r " +
                    "JOIN Books b ON r.book_id = b.book_id " +
                    "JOIN Members m ON r.member_id = m.member_id " +
                    "WHERE r.member_id = ?";
        } else if (user.hasRole("Admin") || user.hasRole("Staff")) {
            query = "SELECT r.*, b.title, m.name FROM Reservations r " +
                    "JOIN Books b ON r.book_id = b.book_id " +
                    "JOIN Members m ON r.member_id = m.member_id";
        } else {
            throw new SecurityException("Unauthorized access");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (user.hasRole("Member")) {
                stmt.setInt(1, user.getMemberId());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(new Reservation(
                            rs.getInt("reservation_id"),
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getInt("member_id"),
                            rs.getString("name"),
                            rs.getDate("reservation_date"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return reservations;
    }

    public void addReservation(User user, int bookId, int memberId) throws SQLException {
        if (user.hasRole("Member") && user.getMemberId() != memberId) {
            throw new SecurityException("Members can only reserve for themselves");
        }
        if (!user.hasRole("Admin") && !user.hasRole("Staff") && !user.hasRole("Member")) {
            throw new SecurityException("Unauthorized access");
        }

        if (hasActiveReservation(user, memberId)) {
            throw new SQLException("Member already has an active reservation. Please return or cancel it first.");
        }

        if (!hasAvailableCopies(bookId)) {
            throw new SQLException("Book is currently unavailable (no copies left).");
        }

        int maxRetries = 3;
        for (int retry = 0; retry < maxRetries; retry++) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                String insertQuery = "INSERT INTO Reservations (book_id, member_id, reservation_date, status) VALUES (?, ?, CURDATE(), 'Active')";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setQueryTimeout(60);
                    stmt.setInt(1, bookId);
                    stmt.setInt(2, memberId);
                    stmt.executeUpdate();
                }

                // Use same connection here to avoid deadlock
                updateBookCopies(conn, bookId, -1);

                conn.commit();
                System.out.println("Reservation added successfully for book_id: " + bookId + ", member_id: " + memberId);
                return;
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                }
                if (retry < maxRetries - 1 && e.getMessage().contains("Lock wait timeout exceeded")) {
                    System.out.println("Retry " + (retry + 1) + " due to lock wait timeout, waiting 1 second...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                System.out.println("SQL Error: " + e.getMessage());
                throw e;
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException closeEx) {
                        closeEx.printStackTrace();
                    }
                }
            }
        }
        throw new SQLException("Failed to add reservation after " + maxRetries + " retries due to lock wait timeout");
    }

    public void cancelReservation(User user, int reservationId) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff") && !user.hasRole("Member")) {
            throw new SecurityException("Unauthorized access");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String selectQuery;
            if (user.hasRole("Member")) {
                selectQuery = "SELECT book_id, status FROM Reservations WHERE reservation_id = ? AND member_id = ? AND status = 'Active'";
            } else {
                selectQuery = "SELECT book_id, status FROM Reservations WHERE reservation_id = ? AND status = 'Active'";
            }

            int bookId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setInt(1, reservationId);
                if (user.hasRole("Member")) {
                    stmt.setInt(2, user.getMemberId());
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        bookId = rs.getInt("book_id");
                    } else {
                        throw new SQLException("Reservation with ID " + reservationId + " not found, not active, or unauthorized access");
                    }
                }
            }

            String updateQuery;
            if (user.hasRole("Member")) {
                updateQuery = "UPDATE Reservations SET status = 'Cancelled' WHERE reservation_id = ? AND member_id = ? AND status = 'Active'";
            } else {
                updateQuery = "UPDATE Reservations SET status = 'Cancelled' WHERE reservation_id = ? AND status = 'Active'";
            }

            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, reservationId);
                if (user.hasRole("Member")) {
                    stmt.setInt(2, user.getMemberId());
                }
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to update reservation status for ID " + reservationId);
                }
            }

            // Use same connection to update book copies
            updateBookCopies(conn, bookId, 1);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            e.printStackTrace();
            throw new SQLException("Error cancelling reservation: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void clearAllReservations(User user) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff")) {
            throw new SecurityException("Only Admin or Staff can clear all reservations");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String selectQuery = "SELECT reservation_id, book_id FROM Reservations WHERE status = 'Active'";
            List<Integer> bookIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookIds.add(rs.getInt("book_id"));
                }
            }

            for (int bookId : bookIds) {
                updateBookCopies(conn, bookId, 1);  // Use same connection
            }

            String updateQuery = "UPDATE Reservations SET status = 'Cancelled' WHERE status = 'Active'";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Error clearing all reservations: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
