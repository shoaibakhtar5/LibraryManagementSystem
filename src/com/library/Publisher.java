package com.library;

public class Publisher {
    private int publisherId;
    private String publisherName;
    private String contactEmail;
    private String address;

    public Publisher(int publisherId, String publisherName, String contactEmail, String address) {
        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.contactEmail = contactEmail;
        this.address = address;
    }

    public int getPublisherId() { return publisherId; }
    public String getPublisherName() { return publisherName; }
    public String getContactEmail() { return contactEmail; }
    public String getAddress() { return address; }

    @Override
    public String toString() { return publisherName; }
}