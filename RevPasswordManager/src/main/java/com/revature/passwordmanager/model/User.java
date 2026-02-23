package com.revature.passwordmanager.model;

/**
 * Represents a user account with authentication and security recovery fields
 */
public class User {
    private int id;
    private String name;
    private String email;
    private String password;           // Hashed login password
    private String masterPassword;     // Hashed master password for vault access
    private String securityQuestion;   // For account recovery
    private String securityAnswer;     // Hashed answer to security question
    
    // Constructor for creating new users (without ID)
    public User(String name, String email, String password, 
                String masterPassword, String securityQuestion, String securityAnswer) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.masterPassword = masterPassword;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }
    
    // Constructor for existing users (with ID from database)
    public User(int id, String name, String email, String password, 
                String masterPassword, String securityQuestion, String securityAnswer) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.masterPassword = masterPassword;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getMasterPassword() { return masterPassword; }
    public String getSecurityQuestion() { return securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setMasterPassword(String masterPassword) { this.masterPassword = masterPassword; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
