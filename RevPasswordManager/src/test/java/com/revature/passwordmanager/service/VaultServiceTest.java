package com.revature.passwordmanager.service;

import com.revature.passwordmanager.model.User;
import com.revature.passwordmanager.model.VaultEntry;
import com.revature.passwordmanager.repository.UserRepository;
import com.revature.passwordmanager.repository.VaultRepository;
import com.revature.passwordmanager.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VaultService
 */
class VaultServiceTest {
    
    @Mock
    private VaultRepository vaultRepository;
    
    @Mock
    private UserRepository userRepository;
    
    private VaultService vaultService;
    private User testUser;
    private String masterPassword;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vaultService = new VaultService(vaultRepository, userRepository);
        
        masterPassword = "master123";
        String hashedMasterPassword = EncryptionUtil.hashPassword(masterPassword);
        testUser = new User(1, "John", "john@example.com", "passHash", 
                          hashedMasterPassword, "Q?", "A");
    }
    
    @Test
    void testVerifyMasterPasswordSuccess() throws Exception {
        // Arrange
        when(userRepository.findById(1)).thenReturn(testUser);
        
        // Act
        boolean result = vaultService.verifyMasterPassword(1, masterPassword);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void testVerifyMasterPasswordFailure() throws Exception {
        // Arrange
        when(userRepository.findById(1)).thenReturn(testUser);
        
        // Act
        boolean result = vaultService.verifyMasterPassword(1, "wrongpassword");
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testAddEntrySuccess() throws Exception {
        // Arrange
        String accountName = "Gmail";
        String username = "john@gmail.com";
        String password = "mypassword";
        String notes = "Personal email";
        
        when(userRepository.findById(1)).thenReturn(testUser);
        when(vaultRepository.findByUserIdAndAccountName(1, accountName)).thenReturn(null);
        when(vaultRepository.save(any(VaultEntry.class))).thenAnswer(invocation -> {
            VaultEntry entry = invocation.getArgument(0);
            entry.setId(1);
            return entry;
        });
        
        // Act
        VaultEntry result = vaultService.addEntry(1, accountName, username, password, 
                                                 notes, masterPassword);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(accountName, result.getAccountName());
        verify(vaultRepository, times(1)).save(any(VaultEntry.class));
    }
    
    @Test
    void testAddEntryWithInvalidMasterPassword() throws Exception {
        // Arrange
        when(userRepository.findById(1)).thenReturn(testUser);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            vaultService.addEntry(1, "Gmail", "user", "pass", "notes", "wrongpassword");
        });
        
        assertTrue(exception.getMessage().contains("Invalid master password"));
        verify(vaultRepository, never()).save(any(VaultEntry.class));
    }
    
    @Test
    void testAddEntryWithDuplicateAccountName() throws Exception {
        // Arrange
        String accountName = "Gmail";
        VaultEntry existingEntry = new VaultEntry(1, accountName, "user", "encPass", "notes");
        
        when(userRepository.findById(1)).thenReturn(testUser);
        when(vaultRepository.findByUserIdAndAccountName(1, accountName)).thenReturn(existingEntry);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            vaultService.addEntry(1, accountName, "user", "pass", "notes", masterPassword);
        });
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(vaultRepository, never()).save(any(VaultEntry.class));
    }
    
    @Test
    void testListEntries() throws Exception {
        // Arrange
        VaultEntry entry1 = new VaultEntry(1, "Gmail", "user1", "encPass1", "notes1");
        VaultEntry entry2 = new VaultEntry(1, "Facebook", "user2", "encPass2", "notes2");
        List<VaultEntry> entries = Arrays.asList(entry1, entry2);
        
        when(vaultRepository.findByUserId(1)).thenReturn(entries);
        
        // Act
        List<VaultEntry> result = vaultService.listEntries(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Gmail", result.get(0).getAccountName());
    }
    
    @Test
    void testViewEntrySuccess() throws Exception {
        // Arrange
        String accountName = "Gmail";
        String originalPassword = "mypassword";
        String encryptedPassword = EncryptionUtil.encryptVaultPassword(originalPassword, masterPassword);
        
        VaultEntry entry = new VaultEntry(1, accountName, "user", encryptedPassword, "notes");
        entry.setId(1);
        
        when(userRepository.findById(1)).thenReturn(testUser);
        when(vaultRepository.findByUserIdAndAccountName(1, accountName)).thenReturn(entry);
        
        // Act
        VaultEntry result = vaultService.viewEntry(1, accountName, masterPassword);
        
        // Assert
        assertNotNull(result);
        assertEquals(accountName, result.getAccountName());
        assertEquals(originalPassword, result.getPassword()); // Should be decrypted
    }
    
    @Test
    void testViewEntryWithInvalidMasterPassword() throws Exception {
        // Arrange
        when(userRepository.findById(1)).thenReturn(testUser);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            vaultService.viewEntry(1, "Gmail", "wrongpassword");
        });
        
        assertTrue(exception.getMessage().contains("Invalid master password"));
    }
    
    @Test
    void testSearchEntries() throws Exception {
        // Arrange
        String searchTerm = "mail";
        VaultEntry entry1 = new VaultEntry(1, "Gmail", "user1", "encPass1", "notes1");
        VaultEntry entry2 = new VaultEntry(1, "Hotmail", "user2", "encPass2", "notes2");
        List<VaultEntry> entries = Arrays.asList(entry1, entry2);
        
        when(vaultRepository.searchByAccountName(1, searchTerm)).thenReturn(entries);
        
        // Act
        List<VaultEntry> result = vaultService.searchEntries(1, searchTerm);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    @Test
    void testUpdateEntrySuccess() throws Exception {
        // Arrange
        String accountName = "Gmail";
        String newPassword = "newpassword";
        String notes = "Updated notes";
        
        VaultEntry entry = new VaultEntry(1, accountName, "user", "oldEncPass", "old notes");
        entry.setId(1);
        
        when(userRepository.findById(1)).thenReturn(testUser);
        when(vaultRepository.findByUserIdAndAccountName(1, accountName)).thenReturn(entry);
        doNothing().when(vaultRepository).update(any(VaultEntry.class));
        
        // Act
        vaultService.updateEntry(1, accountName, newPassword, notes, masterPassword);
        
        // Assert
        verify(vaultRepository, times(1)).update(any(VaultEntry.class));
    }
    
    @Test
    void testUpdateEntryWithInvalidMasterPassword() throws Exception {
        // Arrange
        when(userRepository.findById(1)).thenReturn(testUser);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            vaultService.updateEntry(1, "Gmail", "newpass", "notes", "wrongpassword");
        });
        
        assertTrue(exception.getMessage().contains("Invalid master password"));
        verify(vaultRepository, never()).update(any(VaultEntry.class));
    }
    
    @Test
    void testDeleteEntrySuccess() throws Exception {
        // Arrange
        String accountName = "Gmail";
        
        when(userRepository.findById(1)).thenReturn(testUser);
        when(vaultRepository.delete(1, accountName)).thenReturn(true);
        
        // Act
        vaultService.deleteEntry(1, accountName, masterPassword);
        
        // Assert
        verify(vaultRepository, times(1)).delete(1, accountName);
    }
    
    @Test
    void testDeleteEntryWithInvalidMasterPassword() throws Exception {
        // Arrange
        when(userRepository.findById(1)).thenReturn(testUser);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            vaultService.deleteEntry(1, "Gmail", "wrongpassword");
        });
        
        assertTrue(exception.getMessage().contains("Invalid master password"));
        verify(vaultRepository, never()).delete(anyInt(), anyString());
    }
    
    @Test
    void testDeleteNonExistentEntry() throws Exception {
        // Arrange
        String accountName = "NonExistent";
        
        when(userRepository.findById(1)).thenReturn(testUser);
        when(vaultRepository.delete(1, accountName)).thenReturn(false);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            vaultService.deleteEntry(1, accountName, masterPassword);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
    }
    
    @Test
    void testGetEntryCount() throws Exception {
        // Arrange
        VaultEntry entry1 = new VaultEntry(1, "Gmail", "user1", "encPass1", "notes1");
        VaultEntry entry2 = new VaultEntry(1, "Facebook", "user2", "encPass2", "notes2");
        VaultEntry entry3 = new VaultEntry(1, "Twitter", "user3", "encPass3", "notes3");
        List<VaultEntry> entries = Arrays.asList(entry1, entry2, entry3);
        
        when(vaultRepository.findByUserId(1)).thenReturn(entries);
        
        // Act
        int count = vaultService.getEntryCount(1);
        
        // Assert
        assertEquals(3, count);
    }
}
