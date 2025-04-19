package com.library;

import java.util.Date;

public class Reservation {
    private int reservationId;
    private int bookId;
    private int memberId;
    private Date reservationDate;
    private String status;
    private Date expiryDate;

    public Reservation(int reservationId, int bookId, int memberId, Date reservationDate, String status, Date expiryDate) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.reservationDate = reservationDate;
        this.status = status;
        this.expiryDate = expiryDate;
    }

    // Getters
    public int getReservationId() { return reservationId; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public Date getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
    public Date getExpiryDate() { return expiryDate; }
}