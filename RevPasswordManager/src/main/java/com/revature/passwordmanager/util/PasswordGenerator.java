package com.revature.passwordmanager.util;

import java.security.SecureRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generates strong random passwords based on specified criteria
 */
public class PasswordGenerator {
    private static final Logger logger = LogManager.getLogger(PasswordGenerator.class);
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    
    /**
     * Generate password with default settings (length=16, all character types)
     */
    public static String generate() {
        return generate(16, true, true, true);
    }
    
    /**
     * Generate password with custom settings
     * 
     * @param length Length of the password
     * @param includeUpper Include uppercase letters
     * @param includeDigits Include numbers
     * @param includeSpecial Include special characters
     * @return Generated password
     */
    public static String generate(int length, boolean includeUpper, 
                                 boolean includeDigits, boolean includeSpecial) {
        if (length < 4) {
            logger.warn("Password length too short, using minimum of 4");
            length = 4;
        }
        
        StringBuilder charset = new StringBuilder(LOWERCASE);
        if (includeUpper) charset.append(UPPERCASE);
        if (includeDigits) charset.append(DIGITS);
        if (includeSpecial) charset.append(SPECIAL);
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each selected type
        if (includeUpper && length > 1) {
            password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (includeDigits && length > 2) {
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        if (includeSpecial && length > 3) {
            password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        }
        
        // Always include at least one lowercase
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        
        // Fill the rest randomly
        while (password.length() < length) {
            password.append(charset.charAt(random.nextInt(charset.length())));
        }
        
        // Shuffle the password to randomize character positions
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        String generatedPassword = new String(passwordArray);
        logger.debug("Generated password of length " + length);
        return generatedPassword;
    }
    
    /**
     * Calculate password strength (0-100)
     */
    public static int calculateStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        
        int strength = 0;
        
        // Length bonus
        strength += Math.min(password.length() * 4, 40);
        
        // Character variety bonus
        if (password.matches(".*[a-z].*")) strength += 15;
        if (password.matches(".*[A-Z].*")) strength += 15;
        if (password.matches(".*\\d.*")) strength += 15;
        if (password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*")) strength += 15;
        
        return Math.min(strength, 100);
    }
}
