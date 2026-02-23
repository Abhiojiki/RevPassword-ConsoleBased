package com.revature.passwordmanager.service;

import com.revature.passwordmanager.model.User;
import com.revature.passwordmanager.repository.OTPRepository;
import com.revature.passwordmanager.repository.UserRepository;
import com.revature.passwordmanager.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private OTPRepository otpRepository;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, otpRepository);
    }
    
    @Test
    void testSignupSuccess() throws Exception {
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        String password = "password123";
        String masterPassword = "master123";
        String securityQuestion = "What is your pet's name?";
        String securityAnswer = "Fluffy";
        
        when(userRepository.findByEmail(email)).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1);
            return user;
        });
        
        // Act
        User result = authService.signup(name, email, password, masterPassword, 
                                        securityQuestion, securityAnswer);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testSignupWithExistingEmail() throws Exception {
        // Arrange
        String email = "existing@example.com";
        User existingUser = new User(1, "Existing", email, "hash", "hash", "Q?", "A");
        
        when(userRepository.findByEmail(email)).thenReturn(existingUser);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.signup("New User", email, "password", "master", "Q?", "A");
        });
        
        assertTrue(exception.getMessage().contains("already registered"));
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testSignupWithInvalidEmail() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.signup("John", "invalid-email", "password", "master", "Q?", "A");
        });
    }
    
    @Test
    void testSignupWithShortPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.signup("John", "john@example.com", "short", "master123", "Q?", "A");
        });
    }
    
    @Test
    void testLoginSuccess() throws Exception {
        // Arrange
        String email = "john@example.com";
        String password = "password123";
        String hashedPassword = EncryptionUtil.hashPassword(password);
        
        User user = new User(1, "John", email, hashedPassword, "masterHash", "Q?", "A");
        when(userRepository.findByEmail(email)).thenReturn(user);
        
        // Act
        User result = authService.login(email, password);
        
        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
    }
    
    @Test
    void testLoginWithInvalidPassword() throws Exception {
        // Arrange
        String email = "john@example.com";
        String password = "password123";
        String hashedPassword = EncryptionUtil.hashPassword(password);
        
        User user = new User(1, "John", email, hashedPassword, "masterHash", "Q?", "A");
        when(userRepository.findByEmail(email)).thenReturn(user);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.login(email, "wrongpassword");
        });
        
        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }
    
    @Test
    void testLoginWithNonExistentUser() throws Exception {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.login("nonexistent@example.com", "password");
        });
        
        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }
    
    @Test
    void testGenerateOTP() throws Exception {
        // Arrange
        int userId = 1;
        when(otpRepository.save(any())).thenReturn(null);
        
        // Act
        String otp = authService.generateOTP(userId);
        
        // Assert
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
        verify(otpRepository, times(1)).save(any());
    }
    
    @Test
    void testChangePasswordSuccess() throws Exception {
        // Arrange
        int userId = 1;
        String oldPassword = "oldpass123";
        String newPassword = "newpass123";
        String hashedOldPassword = EncryptionUtil.hashPassword(oldPassword);
        
        User user = new User(userId, "John", "john@example.com", hashedOldPassword, 
                           "masterHash", "Q?", "A");
        when(userRepository.findById(userId)).thenReturn(user);
        doNothing().when(userRepository).update(any(User.class));
        
        // Act
        authService.changePassword(userId, oldPassword, newPassword);
        
        // Assert
        verify(userRepository, times(1)).update(any(User.class));
    }
    
    @Test
    void testChangePasswordWithInvalidOldPassword() throws Exception {
        // Arrange
        int userId = 1;
        String oldPassword = "oldpass123";
        String hashedOldPassword = EncryptionUtil.hashPassword(oldPassword);
        
        User user = new User(userId, "John", "john@example.com", hashedOldPassword, 
                           "masterHash", "Q?", "A");
        when(userRepository.findById(userId)).thenReturn(user);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePassword(userId, "wrongpassword", "newpass123");
        });
        
        assertTrue(exception.getMessage().contains("Invalid old password"));
        verify(userRepository, never()).update(any(User.class));
    }
    
    @Test
    void testUpdateProfile() throws Exception {
        // Arrange
        int userId = 1;
        String newName = "Jane Doe";
        String newEmail = "jane@example.com";
        
        User user = new User(userId, "John", "john@example.com", "hash", "master", "Q?", "A");
        when(userRepository.findById(userId)).thenReturn(user);
        when(userRepository.findByEmail(newEmail)).thenReturn(null);
        doNothing().when(userRepository).update(any(User.class));
        
        // Act
        authService.updateProfile(userId, newName, newEmail);
        
        // Assert
        verify(userRepository, times(1)).update(any(User.class));
    }
}
