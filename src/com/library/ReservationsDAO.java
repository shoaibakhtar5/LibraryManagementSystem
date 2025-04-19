package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationsDAO {
    public void addReservation(User member, int bookId, int memberId) throws SQLException {
        // Check if book is unavailable
        String checkQuery = "SELECT available_copies FROM Books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next() || rs.getInt("available_copies") > 0) {
                throw new SQLException("Book is available or does not exist");
            }
        }

        String query = "INSERT INTO Reservations (book_id, member_id, reservation_date, status, expiry_date) VALUES (?, ?, CURDATE(), 'Active', DATE_ADD(CURDATE(), INTERVAL 7 DAY))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            stmt.executeUpdate();
        }
    }

    public void cancelReservation(int reservationId) throws SQLException {
        String query = "UPDATE Reservations SET status = 'Cancelled' WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            stmt.executeUpdate();
        }
    }

    public List<Reservation> getAllReservations(User staff) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT reservation_id, book_id, member_id, reservation_date, status, expiry_date FROM Reservations";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservations.add(new Reservation(
                        rs.getInt("reservation_id"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id"),
                        rs.getDate("reservation_date"),
                        rs.getString("status"),
                        rs.getDate("expiry_date")
                ));
            }
        }
        return reservations;
    }
}