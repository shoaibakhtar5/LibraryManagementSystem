package com.library;

public class User {
    private int userId;
    private String username;
    private String role;
    private Integer memberId; // Nullable for Admin/Staff
    private Integer staffId;  // Nullable for Admin/Member

    public User(int userId, String username, String role, Integer memberId, Integer staffId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.memberId = memberId;
        this.staffId = staffId;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Integer getMemberId() { return memberId; }
    public Integer getStaffId() { return staffId; }

    // Check if user has a specific role
    public boolean hasRole(String role) {
        return this.role.equalsIgnoreCase(role);
    }
}