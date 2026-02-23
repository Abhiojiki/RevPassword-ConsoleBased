package com.revature.passwordmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a saved password entry in the vault
 */
public class VaultEntry {
    private int id;
    private int userId;
    private String accountName;
    private String username;
    private String password;           // Encrypted password
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor for creating new entries (without ID)
    public VaultEntry(int userId, String accountName, String username, String password, String notes) {
        this.userId = userId;
        this.accountName = accountName;
        this.username = username;
        this.password = password;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor for existing entries (with ID from database)
    public VaultEntry(int id, int userId, String accountName, String username, 
                     String password, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.accountName = accountName;
        this.username = username;
        this.password = password;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getAccountName() { return accountName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setPassword(String password) { 
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public void setUsername(String username) { this.username = username; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "VaultEntry{" +
                "id=" + id +
                ", accountName='" + accountName + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
