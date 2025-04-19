package com.library;

import java.util.Date;

public class Reservation {
    private int reservationId;
    private int bookId;
    private String bookTitle;
    private int memberId;
    private String memberName;
    private Date reservationDate;
    private String status;

    public Reservation(int reservationId, int bookId, String bookTitle, int memberId, String memberName, Date reservationDate, String status) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.memberName = memberName;
        this.reservationDate = reservationDate;
        this.status = status;
    }

    public int getReservationId() { return reservationId; }
    public int getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public int getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public Date getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
}