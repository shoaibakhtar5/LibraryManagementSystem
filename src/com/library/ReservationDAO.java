package com.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
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

        String query = "INSERT INTO Reservations (book_id, member_id, reservation_date, status) VALUES (?, ?, CURDATE(), 'Active')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            stmt.executeUpdate();
        }
    }

    public void cancelReservation(User user, int reservationId) throws SQLException {
        if (!user.hasRole("Admin") && !user.hasRole("Staff") && !user.hasRole("Member")) {
            throw new SecurityException("Unauthorized access");
        }

        String query = user.hasRole("Member") ?
                "UPDATE Reservations SET status = 'Cancelled' WHERE reservation_id = ? AND member_id = ?" :
                "UPDATE Reservations SET status = 'Cancelled' WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            if (user.hasRole("Member")) {
                stmt.setInt(2, user.getMemberId());
            }
            stmt.executeUpdate();
        }
    }
}