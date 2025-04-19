package com.library;

import java.util.Date;

public class Fine {
    private int fineId;
    private int transactionId;
    private int memberId;
    private double amount;
    private String paymentStatus;
    private Date paymentDate;

    public Fine(int fineId, int transactionId, int memberId, double amount, String paymentStatus, Date paymentDate) {
        this.fineId = fineId;
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    // Getters
    public int getFineId() { return fineId; }
    public int getTransactionId() { return transactionId; }
    public int getMemberId() { return memberId; }
    public double getAmount() { return amount; }
    public String getPaymentStatus() { return paymentStatus; }
    public Date getPaymentDate() { return paymentDate; }
}