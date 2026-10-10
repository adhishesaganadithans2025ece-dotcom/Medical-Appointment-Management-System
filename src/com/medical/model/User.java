package com.medical.model;

public class User {
    private final int userId;
    private final String username;
    private final String passwordHash;
    private final String role;
    private final boolean active;

    public User(int userId, String username, String passwordHash, String role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}
