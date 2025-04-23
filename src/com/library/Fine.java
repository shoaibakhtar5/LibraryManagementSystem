package com.library;

import java.sql.Date;

public class Fine {
    private int fineId;
    private int memberId;
    private String memberName;
    private double fineAmount; // Renamed from amount
    private String reason;
    private Date fineDate;
    private String status;
    private Integer transactionId;
    private Integer bookId;
    private String bookTitle;

    public Fine(int fineId, int memberId, String memberName, double fineAmount, String reason, Date fineDate, String status, Integer transactionId, Integer bookId, String bookTitle) {
        this.fineId = fineId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.fineAmount = fineAmount;
        this.reason = reason;
        this.fineDate = fineDate;
        this.status = status;
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
    }

    public int getFineId() {
        return fineId;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public double getAmount() { // Keep this for backward compatibility
        return fineAmount;
    }

    public String getReason() {
        return reason;
    }

    public Date getFineDate() {
        return fineDate;
    }

    public String getStatus() {
        return status;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}