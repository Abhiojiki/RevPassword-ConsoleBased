package com.revature.passwordmanager.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordGenerator
 */
class PasswordGeneratorTest {
    
    @Test
    void testGenerateDefaultPassword() {
        // Act
        String password = PasswordGenerator.generate();
        
        // Assert
        assertNotNull(password);
        assertEquals(16, password.length());
        assertTrue(password.matches(".*[a-z].*"), "Should contain lowercase");
        assertTrue(password.matches(".*[A-Z].*"), "Should contain uppercase");
        assertTrue(password.matches(".*\\d.*"), "Should contain digit");
        assertTrue(password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*"), "Should contain special char");
    }
    
    @Test
    void testGenerateCustomLengthPassword() {
        // Arrange
        int length = 20;
        
        // Act
        String password = PasswordGenerator.generate(length, true, true, true);
        
        // Assert
        assertNotNull(password);
        assertEquals(length, password.length());
    }
    
    @Test
    void testGeneratePasswordWithoutUppercase() {
        // Act
        String password = PasswordGenerator.generate(12, false, true, true);
        
        // Assert
        assertNotNull(password);
        assertEquals(12, password.length());
        assertFalse(password.matches(".*[A-Z].*"), "Should not contain uppercase");
        assertTrue(password.matches(".*[a-z].*"), "Should contain lowercase");
    }
    
    @Test
    void testGeneratePasswordWithoutDigits() {
        // Act
        String password = PasswordGenerator.generate(12, true, false, true);
        
        // Assert
        assertNotNull(password);
        assertEquals(12, password.length());
        assertFalse(password.matches(".*\\d.*"), "Should not contain digits");
    }
    
    @Test
    void testGeneratePasswordWithoutSpecialChars() {
        // Act
        String password = PasswordGenerator.generate(12, true, true, false);
        
        // Assert
        assertNotNull(password);
        assertEquals(12, password.length());
        assertFalse(password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*"), 
                   "Should not contain special characters");
    }
    
    @Test
    void testGeneratePasswordOnlyLowercase() {
        // Act
        String password = PasswordGenerator.generate(10, false, false, false);
        
        // Assert
        assertNotNull(password);
        assertEquals(10, password.length());
        assertTrue(password.matches("[a-z]+"), "Should only contain lowercase");
    }
    
    @Test
    void testGenerateMinimumLengthPassword() {
        // Act - even if we request length 1, it should enforce minimum
        String password = PasswordGenerator.generate(1, true, true, true);
        
        // Assert
        assertNotNull(password);
        assertTrue(password.length() >= 4, "Should enforce minimum length");
    }
    
    @Test
    void testPasswordUniqueness() {
        // Act
        String password1 = PasswordGenerator.generate();
        String password2 = PasswordGenerator.generate();
        
        // Assert
        assertNotEquals(password1, password2, "Generated passwords should be unique");
    }
    
    @Test
    void testCalculateStrengthWeakPassword() {
        // Act
        int strength = PasswordGenerator.calculateStrength("abc");
        
        // Assert
        assertTrue(strength < 40, "Short simple password should have low strength");
    }
    
//    @Test
//    void testCalculateStrengthMediumPassword() {
//        // Act
//        int strength = PasswordGenerator.calculateStrength("password123");
//
//        // Assert
//        assertTrue(strength >= 40 && strength < 70, "Medium password should have medium strength");
//    }
    
    @Test
    void testCalculateStrengthStrongPassword() {
        // Act
        int strength = PasswordGenerator.calculateStrength("MyP@ssw0rd!2024");
        
        // Assert
        assertTrue(strength >= 70, "Complex password should have high strength");
    }
    
    @Test
    void testCalculateStrengthEmptyPassword() {
        // Act
        int strength = PasswordGenerator.calculateStrength("");
        
        // Assert
        assertEquals(0, strength, "Empty password should have zero strength");
    }
    
    @Test
    void testCalculateStrengthNullPassword() {
        // Act
        int strength = PasswordGenerator.calculateStrength(null);
        
        // Assert
        assertEquals(0, strength, "Null password should have zero strength");
    }
    
    @Test
    void testCalculateStrengthMaximum() {
        // Act
        String veryStrongPassword = "MyC0mpl3x!P@ssw0rdW1thM@nyChar$2024";
        int strength = PasswordGenerator.calculateStrength(veryStrongPassword);
        
        // Assert
        assertEquals(100, strength, "Very strong password should reach maximum strength");
    }
}
