package com.revature.passwordmanager.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database configuration and connection management
 */
public class DatabaseConfig {
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private static Properties properties = new Properties();
    
    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.error("Unable to find database.properties");
                throw new RuntimeException("database.properties not found");
            }
            properties.load(input);
            
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC Driver loaded successfully");
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error loading database configuration", e);
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }
    
    /**
     * Get a new database connection
     */
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        
        Connection conn = DriverManager.getConnection(url, username, password);
        logger.debug("Database connection established");
        return conn;
    }
    
    /**
     * Initialize database tables if they don't exist
     */
    public static void initializeDatabase() {
        logger.info("Initializing database tables...");
        
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                master_password VARCHAR(255) NOT NULL,
                security_question VARCHAR(255) NOT NULL,
                security_answer VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        String createVaultEntriesTable = """
            CREATE TABLE IF NOT EXISTS vault_entries (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                account_name VARCHAR(100) NOT NULL,
                username VARCHAR(100),
                password TEXT NOT NULL,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                UNIQUE KEY unique_user_account (user_id, account_name)
            )
        """;
        
        String createOTPTable = """
            CREATE TABLE IF NOT EXISTS otp (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                code VARCHAR(10) NOT NULL,
                expiry_time TIMESTAMP NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;
        
        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {
            
            stmt.execute(createUsersTable);
            logger.info("Users table created/verified");
            
            stmt.execute(createVaultEntriesTable);
            logger.info("Vault entries table created/verified");
            
            stmt.execute(createOTPTable);
            logger.info("OTP table created/verified");
            
            logger.info("Database initialization completed successfully");
            
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
