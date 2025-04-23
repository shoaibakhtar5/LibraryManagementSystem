package com.library;

import java.util.Date;

public class Member {
    private int memberId;
    private String name;
    private String email;
    private String phone;
    private Date joinDate;
    private String address;

    public Member(int memberId, String name, String email, String phone, Date joinDate, String address) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.joinDate = joinDate;
        this.address = address;
    }

    public int getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Date getJoinDate() { return joinDate; }
    public String getAddress() { return address; }

    @Override
    public String toString() {
        return name; // Display the member name in the JComboBox
    }
}