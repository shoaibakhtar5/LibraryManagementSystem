package com.library;

import java.util.Date;

public class Fine {
    private int fineId;
    private int memberId;
    private String memberName;
    private int bookId;
    private String bookTitle;
    private double fineAmount;
    private Date fineDate;
    private String status;

    public Fine(int fineId, int memberId, String memberName, int bookId, String bookTitle, double fineAmount, Date fineDate, String status) {
        this.fineId = fineId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.fineAmount = fineAmount;
        this.fineDate = fineDate;
        this.status = status;
    }

    public int getFineId() { return fineId; }
    public int getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public int getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public double getFineAmount() { return fineAmount; }
    public Date getFineDate() { return fineDate; }
    public String getStatus() { return status; }
}