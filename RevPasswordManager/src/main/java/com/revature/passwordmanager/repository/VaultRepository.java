package com.revature.passwordmanager.repository;

import com.revature.passwordmanager.config.DatabaseConfig;
import com.revature.passwordmanager.model.VaultEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for VaultEntry database operations using JDBC
 */
public class VaultRepository {
    private static final Logger logger = LogManager.getLogger(VaultRepository.class);
    
    /**
     * Save a new vault entry
     */
    public VaultEntry save(VaultEntry entry) throws SQLException {
        String sql = """
            INSERT INTO vault_entries (user_id, account_name, username, password, notes)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, entry.getUserId());
            pstmt.setString(2, entry.getAccountName());
            pstmt.setString(3, entry.getUsername());
            pstmt.setString(4, entry.getPassword());
            pstmt.setString(5, entry.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating vault entry failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entry.setId(generatedKeys.getInt(1));
                    logger.info("Vault entry created successfully with ID: " + entry.getId());
                    return entry;
                } else {
                    throw new SQLException("Creating vault entry failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving vault entry for user: " + entry.getUserId(), e);
            throw e;
        }
    }
    
    /**
     * Find all vault entries for a user
     */
    public List<VaultEntry> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vault_entries WHERE user_id = ? ORDER BY account_name";
        List<VaultEntry> entries = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(extractVaultEntryFromResultSet(rs));
                }
            }
            
            logger.debug("Retrieved " + entries.size() + " vault entries for user: " + userId);
            return entries;
        } catch (SQLException e) {
            logger.error("Error finding vault entries for user: " + userId, e);
            throw e;
        }
    }
    
    /**
     * Find vault entry by user ID and account name
     */
    public VaultEntry findByUserIdAndAccountName(int userId, String accountName) throws SQLException {
        String sql = "SELECT * FROM vault_entries WHERE user_id = ? AND account_name = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, accountName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    VaultEntry entry = extractVaultEntryFromResultSet(rs);
                    logger.debug("Vault entry found: " + accountName);
                    return entry;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding vault entry: " + accountName, e);
            throw e;
        }
        
        logger.debug("Vault entry not found: " + accountName);
        return null;
    }
    
    /**
     * Search vault entries by account name (partial match)
     */
    public List<VaultEntry> searchByAccountName(int userId, String searchTerm) throws SQLException {
        String sql = "SELECT * FROM vault_entries WHERE user_id = ? AND account_name LIKE ? ORDER BY account_name";
        List<VaultEntry> entries = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, "%" + searchTerm + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(extractVaultEntryFromResultSet(rs));
                }
            }
            
            logger.debug("Found " + entries.size() + " vault entries matching: " + searchTerm);
            return entries;
        } catch (SQLException e) {
            logger.error("Error searching vault entries: " + searchTerm, e);
            throw e;
        }
    }
    
    /**
     * Update vault entry
     */
    public void update(VaultEntry entry) throws SQLException {
        String sql = """
            UPDATE vault_entries 
            SET account_name = ?, username = ?, password = ?, notes = ?, updated_at = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, entry.getAccountName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setString(3, entry.getPassword());
            pstmt.setString(4, entry.getNotes());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(6, entry.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Vault entry updated successfully: " + entry.getId());
            } else {
                logger.warn("No vault entry found to update with ID: " + entry.getId());
            }
        } catch (SQLException e) {
            logger.error("Error updating vault entry: " + entry.getId(), e);
            throw e;
        }
    }
    
    /**
     * Delete vault entry by user ID and account name
     */
    public boolean delete(int userId, String accountName) throws SQLException {
        String sql = "DELETE FROM vault_entries WHERE user_id = ? AND account_name = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, accountName);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Vault entry deleted: " + accountName);
                return true;
            }
            
            logger.warn("No vault entry found to delete: " + accountName);
            return false;
        } catch (SQLException e) {
            logger.error("Error deleting vault entry: " + accountName, e);
            throw e;
        }
    }
    
    /**
     * Delete vault entry by ID
     */
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM vault_entries WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Vault entry deleted with ID: " + id);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            logger.error("Error deleting vault entry by ID: " + id, e);
            throw e;
        }
    }
    
    /**
     * Extract VaultEntry object from ResultSet
     */
    private VaultEntry extractVaultEntryFromResultSet(ResultSet rs) throws SQLException {
        return new VaultEntry(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("account_name"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("notes"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
