package com.revature.passwordmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a one-time password for verification
 */
public class OTP {
    private int id;
    private int userId;
    private String code;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt;
    
    // Constructor for creating new OTP (without ID)
    public OTP(int userId, String code, LocalDateTime expiryTime) {
        this.userId = userId;
        this.code = code;
        this.expiryTime = expiryTime;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor for existing OTP (with ID from database)
    public OTP(int id, int userId, String code, LocalDateTime expiryTime, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.expiryTime = expiryTime;
        this.createdAt = createdAt;
    }
    
    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getCode() { return code; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    
    // Check if OTP is still valid (not expired)
    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiryTime);
    }
    
    @Override
    public String toString() {
        return "OTP{" +
                "id=" + id +
                ", userId=" + userId +
                ", code='" + code + '\'' +
                ", expiryTime=" + expiryTime +
                ", isValid=" + isValid() +
                '}';
    }
}
