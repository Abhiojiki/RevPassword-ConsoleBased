package com.revature.passwordmanager.service;

import com.revature.passwordmanager.model.User;
import com.revature.passwordmanager.model.VaultEntry;
import com.revature.passwordmanager.repository.UserRepository;
import com.revature.passwordmanager.repository.VaultRepository;
import com.revature.passwordmanager.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for vault/password management operations with master password protection
 */
public class VaultService {
    private static final Logger logger = LogManager.getLogger(VaultService.class);
    private final VaultRepository vaultRepo;
    private final UserRepository userRepo;
    
    public VaultService(VaultRepository vaultRepo, UserRepository userRepo) {
        this.vaultRepo = vaultRepo;
        this.userRepo = userRepo;
    }
    
    /**
     * Verify master password before allowing sensitive operations
     */
    public boolean verifyMasterPassword(int userId, String masterPassword) throws Exception {
        logger.debug("Verifying master password for user: " + userId);
        
        if (masterPassword == null || masterPassword.trim().isEmpty()) {
            return false;
        }
        
        try {
            User user = userRepo.findById(userId);
            if (user == null) {
                throw new Exception("User not found");
            }
            
            boolean matches = EncryptionUtil.verifyPassword(masterPassword, user.getMasterPassword());
            
            if (matches) {
                logger.debug("Master password verified for user: " + userId);
            } else {
                logger.warn("Master password verification failed for user: " + userId);
            }
            
            return matches;
        } catch (SQLException e) {
            logger.error("Error verifying master password", e);
            throw new Exception("Failed to verify master password: " + e.getMessage());
        }
    }
    
    /**
     * Add new password entry with encryption
     */
    public VaultEntry addEntry(int userId, String accountName, String username, 
                              String password, String notes, String masterPassword) throws Exception {
        logger.info("Adding vault entry for user: " + userId + ", account: " + accountName);
        
        // Validate input
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        // Verify master password
        if (!verifyMasterPassword(userId, masterPassword)) {
            throw new Exception("Invalid master password");
        }
        
        try {
            // Check if account already exists
            VaultEntry existing = vaultRepo.findByUserIdAndAccountName(userId, accountName);
            if (existing != null) {
                throw new Exception("Account name already exists in vault");
            }
            
            // Encrypt password using master password
            String encryptedPassword = EncryptionUtil.encryptVaultPassword(password, masterPassword);
            
            VaultEntry entry = new VaultEntry(userId, accountName, username, encryptedPassword, notes);
            VaultEntry savedEntry = vaultRepo.save(entry);
            
            logger.info("Vault entry added successfully: " + accountName);
            return savedEntry;
        } catch (SQLException e) {
            logger.error("Error adding vault entry", e);
            throw new Exception("Failed to add vault entry: " + e.getMessage());
        }
    }
    
    /**
     * List all account names (passwords hidden)
     */
    public List<VaultEntry> listEntries(int userId) throws Exception {
        logger.info("Listing vault entries for user: " + userId);
        
        try {
            List<VaultEntry> entries = vaultRepo.findByUserId(userId);
            logger.info("Retrieved " + entries.size() + " vault entries");
            return entries;
        } catch (SQLException e) {
            logger.error("Error listing vault entries", e);
            throw new Exception("Failed to list vault entries: " + e.getMessage());
        }
    }
    
    /**
     * View password details with decryption - requires master password
     */
    public VaultEntry viewEntry(int userId, String accountName, String masterPassword) throws Exception {
        logger.info("Viewing vault entry: " + accountName + " for user: " + userId);
        
        if (!verifyMasterPassword(userId, masterPassword)) {
            throw new Exception("Invalid master password");
        }
        
        try {
            VaultEntry entry = vaultRepo.findByUserIdAndAccountName(userId, accountName);
            if (entry == null) {
                throw new Exception("Account not found in vault");
            }
            
            // Decrypt password
            String decryptedPassword = EncryptionUtil.decryptVaultPassword(entry.getPassword(), masterPassword);
            
            // Create a copy with decrypted password for display
            VaultEntry decryptedEntry = new VaultEntry(
                entry.getId(),
                entry.getUserId(),
                entry.getAccountName(),
                entry.getUsername(),
                decryptedPassword,  // Decrypted password
                entry.getNotes(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
            );
            
            logger.info("Vault entry viewed successfully: " + accountName);
            return decryptedEntry;
        } catch (SQLException e) {
            logger.error("Error viewing vault entry", e);
            throw new Exception("Failed to view vault entry: " + e.getMessage());
        }
    }
    
    /**
     * Search for entries by account name
     */
    public List<VaultEntry> searchEntries(int userId, String searchTerm) throws Exception {
        logger.info("Searching vault entries for: " + searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }
        
        try {
            List<VaultEntry> entries = vaultRepo.searchByAccountName(userId, searchTerm);
            logger.info("Found " + entries.size() + " matching entries");
            return entries;
        } catch (SQLException e) {
            logger.error("Error searching vault entries", e);
            throw new Exception("Failed to search vault entries: " + e.getMessage());
        }
    }
    
    /**
     * Update existing password with re-encryption
     */
    public void updateEntry(int userId, String accountName, String newPassword, 
                          String notes, String masterPassword) throws Exception {
        logger.info("Updating vault entry: " + accountName + " for user: " + userId);
        
        if (!verifyMasterPassword(userId, masterPassword)) {
            throw new Exception("Invalid master password");
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        try {
            VaultEntry entry = vaultRepo.findByUserIdAndAccountName(userId, accountName);
            if (entry == null) {
                throw new Exception("Account not found in vault");
            }
            
            // Encrypt new password
            String encryptedPassword = EncryptionUtil.encryptVaultPassword(newPassword, masterPassword);
            entry.setPassword(encryptedPassword);
            
            if (notes != null) {
                entry.setNotes(notes);
            }
            
            vaultRepo.update(entry);
            logger.info("Vault entry updated successfully: " + accountName);
        } catch (SQLException e) {
            logger.error("Error updating vault entry", e);
            throw new Exception("Failed to update vault entry: " + e.getMessage());
        }
    }
    
    /**
     * Delete password entry - requires master password
     */
    public void deleteEntry(int userId, String accountName, String masterPassword) throws Exception {
        logger.info("Deleting vault entry: " + accountName + " for user: " + userId);
        
        if (!verifyMasterPassword(userId, masterPassword)) {
            throw new Exception("Invalid master password");
        }
        
        try {
            boolean deleted = vaultRepo.delete(userId, accountName);
            if (!deleted) {
                throw new Exception("Account not found in vault");
            }
            
            logger.info("Vault entry deleted successfully: " + accountName);
        } catch (SQLException e) {
            logger.error("Error deleting vault entry", e);
            throw new Exception("Failed to delete vault entry: " + e.getMessage());
        }
    }
    
    /**
     * Get count of vault entries for a user
     */
    public int getEntryCount(int userId) throws Exception {
        try {
            List<VaultEntry> entries = vaultRepo.findByUserId(userId);
            return entries.size();
        } catch (SQLException e) {
            logger.error("Error getting entry count", e);
            throw new Exception("Failed to get entry count: " + e.getMessage());
        }
    }
}
