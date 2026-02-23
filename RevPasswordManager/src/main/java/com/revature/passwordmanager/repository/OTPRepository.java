package com.revature.passwordmanager.repository;

import com.revature.passwordmanager.config.DatabaseConfig;
import com.revature.passwordmanager.model.OTP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Repository for OTP database operations using JDBC
 */
public class OTPRepository {
    private static final Logger logger = LogManager.getLogger(OTPRepository.class);
    
    /**
     * Save a new OTP
     */
    public OTP save(OTP otp) throws SQLException {
        // First, delete any existing OTP for this user
        delete(otp.getUserId());
        
        String sql = """
            INSERT INTO otp (user_id, code, expiry_time)
            VALUES (?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, otp.getUserId());
            pstmt.setString(2, otp.getCode());
            pstmt.setTimestamp(3, Timestamp.valueOf(otp.getExpiryTime()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating OTP failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    otp.setId(generatedKeys.getInt(1));
                    logger.info("OTP created successfully for user: " + otp.getUserId());
                    return otp;
                } else {
                    throw new SQLException("Creating OTP failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving OTP for user: " + otp.getUserId(), e);
            throw e;
        }
    }
    
    /**
     * Find OTP by user ID
     */
    public OTP findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM otp WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    OTP otp = extractOTPFromResultSet(rs);
                    logger.debug("OTP found for user: " + userId);
                    return otp;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding OTP for user: " + userId, e);
            throw e;
        }
        
        logger.debug("No OTP found for user: " + userId);
        return null;
    }
    
    /**
     * Delete OTP by user ID
     */
    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM otp WHERE user_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("OTP deleted for user: " + userId);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            logger.error("Error deleting OTP for user: " + userId, e);
            throw e;
        }
    }
    
    /**
     * Clean up expired OTPs (can be called periodically)
     */
    public int deleteExpiredOTPs() throws SQLException {
        String sql = "DELETE FROM otp WHERE expiry_time < ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Cleaned up " + affectedRows + " expired OTPs");
            }
            
            return affectedRows;
        } catch (SQLException e) {
            logger.error("Error cleaning up expired OTPs", e);
            throw e;
        }
    }
    
    /**
     * Extract OTP object from ResultSet
     */
    private OTP extractOTPFromResultSet(ResultSet rs) throws SQLException {
        return new OTP(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("code"),
            rs.getTimestamp("expiry_time").toLocalDateTime(),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
