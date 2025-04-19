package com.library;

import java.util.Date;

public class Staff {
    private int staffId;
    private String name;
    private String email;
    private String phone;
    private Date hireDate;
    private String role;

    public Staff(int staffId, String name, String email, String phone, Date hireDate, String role) {
        this.staffId = staffId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.hireDate = hireDate;
        this.role = role;
    }

    // Getters
    public int getStaffId() { return staffId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Date getHireDate() { return hireDate; }
    public String getRole() { return role; }
}