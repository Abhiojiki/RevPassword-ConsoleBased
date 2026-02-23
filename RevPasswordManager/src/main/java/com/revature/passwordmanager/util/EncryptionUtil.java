package com.revature.passwordmanager.util;

import org.mindrot.jbcrypt.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utility class for password hashing and vault entry encryption
 */
public class EncryptionUtil {
    private static final Logger logger = LogManager.getLogger(EncryptionUtil.class);
    private static final String ALGORITHM = "AES";
    
    /**
     * Hash a password using BCrypt (for user passwords and master passwords)
     */
    public static String hashPassword(String password) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
        logger.debug("Password hashed successfully");
        return hashed;
    }
    
    /**
     * Verify a password against a hash
     */
    public static boolean verifyPassword(String password, String hash) {
        boolean matches = BCrypt.checkpw(password, hash);
        logger.debug("Password verification: " + (matches ? "success" : "failed"));
        return matches;
    }
    
    /**
     * Encrypt vault password using AES (master password as key)
     */
    public static String encryptVaultPassword(String password, String masterPassword) {
        try {
            SecretKeySpec secretKey = createKey(masterPassword);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(encrypted);
            logger.debug("Vault password encrypted successfully");
            return result;
        } catch (Exception e) {
            logger.error("Error encrypting vault password", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypt vault password using AES (master password as key)
     */
    public static String decryptVaultPassword(String encryptedPassword, String masterPassword) {
        try {
            SecretKeySpec secretKey = createKey(masterPassword);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            String result = new String(decrypted, StandardCharsets.UTF_8);
            logger.debug("Vault password decrypted successfully");
            return result;
        } catch (Exception e) {
            logger.error("Error decrypting vault password", e);
            throw new RuntimeException("Decryption failed - invalid master password or corrupted data", e);
        }
    }
    
    /**
     * Create AES key from master password
     */
    private static SecretKeySpec createKey(String masterPassword) {
        try {
            byte[] key = masterPassword.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // Use first 128 bits
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            logger.error("Error creating encryption key", e);
            throw new RuntimeException("Failed to create encryption key", e);
        }
    }
}
