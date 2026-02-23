package com.revature.passwordmanager.repository;

import com.revature.passwordmanager.config.DatabaseConfig;
import com.revature.passwordmanager.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for User database operations using JDBC
 */
public class UserRepository {
    private static final Logger logger = LogManager.getLogger(UserRepository.class);
    
    /**
     * Save a new user to the database
     */
    public User save(User user) throws SQLException {
        String sql = """
            INSERT INTO users (name, email, password, master_password, security_question, security_answer)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getMasterPassword());
            pstmt.setString(5, user.getSecurityQuestion());
            pstmt.setString(6, user.getSecurityAnswer());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                    logger.info("User created successfully with ID: " + user.getId());
                    return user;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving user: " + user.getEmail(), e);
            throw e;
        }
    }
    
    /**
     * Find user by email
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = extractUserFromResultSet(rs);
                    logger.debug("User found: " + email);
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: " + email, e);
            throw e;
        }
        
        logger.debug("User not found: " + email);
        return null;
    }
    
    /**
     * Find user by ID
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = extractUserFromResultSet(rs);
                    logger.debug("User found with ID: " + id);
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by ID: " + id, e);
            throw e;
        }
        
        logger.debug("User not found with ID: " + id);
        return null;
    }
    
    /**
     * Update user information
     */
    public void update(User user) throws SQLException {
        String sql = """
            UPDATE users 
            SET name = ?, email = ?, password = ?, master_password = ?, 
                security_question = ?, security_answer = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getMasterPassword());
            pstmt.setString(5, user.getSecurityQuestion());
            pstmt.setString(6, user.getSecurityAnswer());
            pstmt.setInt(7, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("User updated successfully: " + user.getId());
            } else {
                logger.warn("No user found to update with ID: " + user.getId());
            }
        } catch (SQLException e) {
            logger.error("Error updating user: " + user.getId(), e);
            throw e;
        }
    }
    
    /**
     * Get all users (for admin purposes)
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            
            logger.debug("Retrieved " + users.size() + " users");
            return users;
        } catch (SQLException e) {
            logger.error("Error retrieving all users", e);
            throw e;
        }
    }
    
    /**
     * Delete user by ID
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("User deleted successfully: " + id);
                return true;
            }
            
            logger.warn("No user found to delete with ID: " + id);
            return false;
        } catch (SQLException e) {
            logger.error("Error deleting user: " + id, e);
            throw e;
        }
    }
    
    /**
     * Extract User object from ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("master_password"),
            rs.getString("security_question"),
            rs.getString("security_answer")
        );
    }
}
