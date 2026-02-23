package com.revature.passwordmanager.service;

import com.revature.passwordmanager.model.OTP;
import com.revature.passwordmanager.model.User;
import com.revature.passwordmanager.repository.OTPRepository;
import com.revature.passwordmanager.repository.UserRepository;
import com.revature.passwordmanager.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Service layer for authentication and account recovery operations
 */
public class AuthService {
    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private final UserRepository userRepo;
    private final OTPRepository otpRepo;
    
    public AuthService(UserRepository userRepo, OTPRepository otpRepo) {
        this.userRepo = userRepo;
        this.otpRepo = otpRepo;
    }
    
    /**
     * Create new user account with security features
     */
    public User signup(String name, String email, String password, 
                      String masterPassword, String securityQuestion, 
                      String securityAnswer) throws Exception {
        logger.info("Attempting to create new user: " + email);
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (masterPassword == null || masterPassword.length() < 6) {
            throw new IllegalArgumentException("Master password must be at least 6 characters");
        }
        if (securityQuestion == null || securityQuestion.trim().isEmpty()) {
            throw new IllegalArgumentException("Security question cannot be empty");
        }
        if (securityAnswer == null || securityAnswer.trim().isEmpty()) {
            throw new IllegalArgumentException("Security answer cannot be empty");
        }
        
        try {
            // Check if email already exists
            if (userRepo.findByEmail(email) != null) {
                logger.warn("Signup attempt with existing email: " + email);
                throw new Exception("Email already registered");
            }
            
            // Hash passwords and security answer
            String hashedPassword = EncryptionUtil.hashPassword(password);
            String hashedMasterPassword = EncryptionUtil.hashPassword(masterPassword);
            String hashedSecurityAnswer = EncryptionUtil.hashPassword(securityAnswer.toLowerCase().trim());
            
            User user = new User(name, email, hashedPassword, hashedMasterPassword, 
                                securityQuestion, hashedSecurityAnswer);
            
            User savedUser = userRepo.save(user);
            logger.info("User created successfully: " + email);
            return savedUser;
        } catch (SQLException e) {
            logger.error("Database error during signup", e);
            throw new Exception("Failed to create account: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user and return user object if successful
     */
    public User login(String email, String password) throws Exception {
        logger.info("Login attempt for: " + email);
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        try {
            User user = userRepo.findByEmail(email);
            
            if (user == null) {
                logger.warn("Login failed - user not found: " + email);
                throw new Exception("Invalid email or password");
            }
            
            if (!EncryptionUtil.verifyPassword(password, user.getPassword())) {
                logger.warn("Login failed - invalid password for: " + email);
                throw new Exception("Invalid email or password");
            }
            
            logger.info("Login successful: " + email);
            return user;
        } catch (SQLException e) {
            logger.error("Database error during login", e);
            throw new Exception("Login failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate a 6-digit OTP code that expires in 5 minutes
     */
    public String generateOTP(int userId) throws Exception {
        logger.info("Generating OTP for user ID: " + userId);
        
        try {
            SecureRandom random = new SecureRandom();
            String code = String.format("%06d", random.nextInt(1000000));
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
            
            OTP otp = new OTP(userId, code, expiry);
            otpRepo.save(otp);
            
            logger.info("OTP generated successfully for user: " + userId);
            return code;
        } catch (SQLException e) {
            logger.error("Error generating OTP", e);
            throw new Exception("Failed to generate OTP: " + e.getMessage());
        }
    }
    
    /**
     * Verify if the provided OTP code is correct and not expired
     */
    public boolean verifyOTP(int userId, String code) throws Exception {
        logger.info("Verifying OTP for user ID: " + userId);
        
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        try {
            OTP otp = otpRepo.findByUserId(userId);
            
            if (otp == null) {
                logger.warn("No OTP found for user: " + userId);
                return false;
            }
            
            if (!otp.isValid()) {
                logger.warn("OTP expired for user: " + userId);
                otpRepo.delete(userId);
                return false;
            }
            
            if (otp.getCode().equals(code)) {
                otpRepo.delete(userId);  // OTP is single-use
                logger.info("OTP verified successfully for user: " + userId);
                return true;
            }
            
            logger.warn("Invalid OTP code for user: " + userId);
            return false;
        } catch (SQLException e) {
            logger.error("Error verifying OTP", e);
            throw new Exception("Failed to verify OTP: " + e.getMessage());
        }
    }
    
    /**
     * Verify security answer for account recovery
     */
    public boolean verifySecurityAnswer(String email, String answer) throws Exception {
        logger.info("Verifying security answer for: " + email);
        
        try {
            User user = userRepo.findByEmail(email);
            if (user == null) {
                logger.warn("User not found for security answer verification: " + email);
                throw new Exception("User not found");
            }
            
            boolean matches = EncryptionUtil.verifyPassword(answer.toLowerCase().trim(), 
                                                           user.getSecurityAnswer());
            
            if (matches) {
                logger.info("Security answer verified successfully for: " + email);
            } else {
                logger.warn("Security answer verification failed for: " + email);
            }
            
            return matches;
        } catch (SQLException e) {
            logger.error("Error verifying security answer", e);
            throw new Exception("Failed to verify security answer: " + e.getMessage());
        }
    }
    
    /**
     * Reset password using security question verification
     */
    public void resetPassword(String email, String newPassword) throws Exception {
        logger.info("Resetting password for: " + email);
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        try {
            User user = userRepo.findByEmail(email);
            if (user == null) {
                throw new Exception("User not found");
            }
            
            String hashedPassword = EncryptionUtil.hashPassword(newPassword);
            user.setPassword(hashedPassword);
            userRepo.update(user);
            
            logger.info("Password reset successfully for: " + email);
        } catch (SQLException e) {
            logger.error("Error resetting password", e);
            throw new Exception("Failed to reset password: " + e.getMessage());
        }
    }
    
    /**
     * Change user password (requires old password verification)
     */
    public void changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        logger.info("Changing password for user ID: " + userId);
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        
        try {
            User user = userRepo.findById(userId);
            if (user == null) {
                throw new Exception("User not found");
            }
            
            if (!EncryptionUtil.verifyPassword(oldPassword, user.getPassword())) {
                logger.warn("Old password verification failed for user: " + userId);
                throw new Exception("Invalid old password");
            }
            
            String hashedPassword = EncryptionUtil.hashPassword(newPassword);
            user.setPassword(hashedPassword);
            userRepo.update(user);
            
            logger.info("Password changed successfully for user: " + userId);
        } catch (SQLException e) {
            logger.error("Error changing password", e);
            throw new Exception("Failed to change password: " + e.getMessage());
        }
    }
    
    /**
     * Get security question for a user
     */
    public String getSecurityQuestion(String email) throws Exception {
        logger.info("Retrieving security question for: " + email);
        
        try {
            User user = userRepo.findByEmail(email);
            if (user == null) {
                throw new Exception("User not found");
            }
            return user.getSecurityQuestion();
        } catch (SQLException e) {
            logger.error("Error retrieving security question", e);
            throw new Exception("Failed to retrieve security question: " + e.getMessage());
        }
    }
    
    /**
     * Update user profile information
     */
    public void updateProfile(int userId, String newName, String newEmail) throws Exception {
        logger.info("Updating profile for user ID: " + userId);
        
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        
        try {
            User user = userRepo.findById(userId);
            if (user == null) {
                throw new Exception("User not found");
            }
            
            user.setName(newName);
            
            if (newEmail != null && !newEmail.equals(user.getEmail())) {
                // Check if new email is already taken
                User existingUser = userRepo.findByEmail(newEmail);
                if (existingUser != null && existingUser.getId() != userId) {
                    throw new Exception("Email already in use");
                }
                user.setEmail(newEmail);
            }
            
            userRepo.update(user);
            logger.info("Profile updated successfully for user: " + userId);
        } catch (SQLException e) {
            logger.error("Error updating profile", e);
            throw new Exception("Failed to update profile: " + e.getMessage());
        }
    }
    
    /**
     * Update security question and answer
     */
    public void updateSecurityQuestion(int userId, String newQuestion, String newAnswer) throws Exception {
        logger.info("Updating security question for user ID: " + userId);
        
        if (newQuestion == null || newQuestion.trim().isEmpty()) {
            throw new IllegalArgumentException("Security question cannot be empty");
        }
        if (newAnswer == null || newAnswer.trim().isEmpty()) {
            throw new IllegalArgumentException("Security answer cannot be empty");
        }
        
        try {
            User user = userRepo.findById(userId);
            if (user == null) {
                throw new Exception("User not found");
            }
            
            String hashedAnswer = EncryptionUtil.hashPassword(newAnswer.toLowerCase().trim());
            user.setSecurityQuestion(newQuestion);
            user.setSecurityAnswer(hashedAnswer);
            
            userRepo.update(user);
            logger.info("Security question updated successfully for user: " + userId);
        } catch (SQLException e) {
            logger.error("Error updating security question", e);
            throw new Exception("Failed to update security question: " + e.getMessage());
        }
    }
}
