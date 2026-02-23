package com.revature.passwordmanager;

import com.revature.passwordmanager.config.DatabaseConfig;
import com.revature.passwordmanager.model.User;
import com.revature.passwordmanager.model.VaultEntry;
import com.revature.passwordmanager.repository.OTPRepository;
import com.revature.passwordmanager.repository.UserRepository;
import com.revature.passwordmanager.repository.VaultRepository;
import com.revature.passwordmanager.service.AuthService;
import com.revature.passwordmanager.service.VaultService;
import com.revature.passwordmanager.util.PasswordGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Scanner;

/**
 * Main application class with console-based user interface
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Scanner scanner = new Scanner(System.in);
    
    private static AuthService authService;
    private static VaultService vaultService;
    private static User currentUser;
    
    public static void main(String[] args) {
        try {
            // Initialize application
            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║   RevPassword Manager Application       ║");
            System.out.println("║   Secure Password Management Solution    ║");
            System.out.println("╚══════════════════════════════════════════╝\n");
            
            logger.info("Application starting...");
            
            // Initialize database
            DatabaseConfig.initializeDatabase();
            
            // Initialize repositories
            UserRepository userRepo = new UserRepository();
            VaultRepository vaultRepo = new VaultRepository();
            OTPRepository otpRepo = new OTPRepository();
           // Initialize services
            authService = new AuthService(userRepo, otpRepo);
            vaultService = new VaultService(vaultRepo, userRepo);
            
            logger.info("Application initialized successfully");
            
            // Run main application loop
            runMainLoop();
            
        } catch (Exception e) {
            logger.error("Fatal error in application", e);
            System.err.println("\n❌ Fatal Error: " + e.getMessage());
            System.err.println("Please check the logs for more details.");
        } finally {
            scanner.close();
            logger.info("Application terminated");
        }
    }
    
    private static void runMainLoop() {
        boolean running = true;
        
        while (running) {
            if (currentUser == null) {
                // Not logged in - show authentication menu
                running = showAuthMenu();
            } else {
                // Logged in - show vault menu
                running = showVaultMenu();
            }
        }
        
        System.out.println("\n👋 Thank you for using RevPassword Manager. Goodbye!");
    }
    
    private static boolean showAuthMenu() {
        System.out.println("\n┌──────────────────────────────┐");
        System.out.println("│    Authentication Menu       │");
        System.out.println("└──────────────────────────────┘");
        System.out.println("1. Create Account (Sign Up)");
        System.out.println("2. Login");
        System.out.println("3. Forgot Password");
        System.out.println("0. Exit Application");
        System.out.print("\nSelect option: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            switch (choice) {
                case 1:
                    handleSignup();
                    break;
                case 2:
                    handleLogin();
                    break;
                case 3:
                    handleForgotPassword();
                    break;
                case 0:
                    return false;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number.");
        }
        
        return true;
    }
    
    private static void handleSignup() {
        System.out.println("\n═══ Create New Account ═══");
        
        try {
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            
            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("Enter login password (min 6 chars): ");
            String password = scanner.nextLine().trim();
            
            System.out.print("Confirm login password: ");
            String confirmPassword = scanner.nextLine().trim();
            
            if (!password.equals(confirmPassword)) {
                System.out.println("❌ Passwords do not match!");
                return;
            }
            
            System.out.print("Enter master password for vault (min 6 chars): ");
            String masterPassword = scanner.nextLine().trim();
            
            System.out.print("Confirm master password: ");
            String confirmMasterPassword = scanner.nextLine().trim();
            
            if (!masterPassword.equals(confirmMasterPassword)) {
                System.out.println("❌ Master passwords do not match!");
                return;
            }
            
            System.out.print("Enter security question: ");
            String securityQuestion = scanner.nextLine().trim();
            
            System.out.print("Enter security answer: ");
            String securityAnswer = scanner.nextLine().trim();
            
            User user = authService.signup(name, email, password, masterPassword, 
                                          securityQuestion, securityAnswer);
            
            System.out.println("\n✅ Account created successfully!");
            System.out.println("Welcome, " + user.getName() + "!");
            logger.info("New user registered: " + email);
            
        } catch (Exception e) {
            System.out.println("❌ Signup failed: " + e.getMessage());
            logger.error("Signup error", e);
        }
    }
    
    private static void handleLogin() {
        System.out.println("\n═══ Login ═══");
        
        try {
            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();
            
            currentUser = authService.login(email, password);
            
            System.out.println("\n✅ Login successful!");
            System.out.println("Welcome back, " + currentUser.getName() + "!");
            logger.info("User logged in: " + email);
            
        } catch (Exception e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            logger.error("Login error", e);
        }
    }
    
    private static void handleForgotPassword() {
        System.out.println("\n═══ Password Recovery ═══");
        
        try {
            System.out.print("Enter your email: ");
            String email = scanner.nextLine().trim();
            
            String securityQuestion = authService.getSecurityQuestion(email);
            System.out.println("\nSecurity Question: " + securityQuestion);
            
            System.out.print("Enter your answer: ");
            String answer = scanner.nextLine().trim();
            
            if (authService.verifySecurityAnswer(email, answer)) {
                System.out.print("\nEnter new password: ");
                String newPassword = scanner.nextLine().trim();
                
                System.out.print("Confirm new password: ");
                String confirmPassword = scanner.nextLine().trim();
                
                if (!newPassword.equals(confirmPassword)) {
                    System.out.println("❌ Passwords do not match!");
                    return;
                }
                
                authService.resetPassword(email, newPassword);
                System.out.println("\n✅ Password reset successfully!");
                logger.info("Password reset for user: " + email);
            } else {
                System.out.println("❌ Incorrect security answer!");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Password recovery failed: " + e.getMessage());
            logger.error("Password recovery error", e);
        }
    }
    
    private static boolean showVaultMenu() {
        System.out.println("\n┌──────────────────────────────────────┐");
        System.out.println("│    Password Vault - " + currentUser.getName());
        System.out.println("└──────────────────────────────────────┘");
        System.out.println("1. Add Password Entry");
        System.out.println("2. List All Passwords");
        System.out.println("3. View Password Details");
        System.out.println("4. Search Passwords");
        System.out.println("5. Update Password");
        System.out.println("6. Delete Password");
        System.out.println("7. Generate Random Password");
        System.out.println("8. Change Login Password");
        System.out.println("9. Update Profile");
        System.out.println("10. Generate OTP (Verification Code)");
        System.out.println("0. Logout");
        System.out.print("\nSelect option: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            switch (choice) {
                case 1:
                    handleAddEntry();
                    break;
                case 2:
                    handleListEntries();
                    break;
                case 3:
                    handleViewEntry();
                    break;
                case 4:
                    handleSearchEntries();
                    break;
                case 5:
                    handleUpdateEntry();
                    break;
                case 6:
                    handleDeleteEntry();
                    break;
                case 7:
                    handleGeneratePassword();
                    break;
                case 8:
                    handleChangePassword();
                    break;
                case 9:
                    handleUpdateProfile();
                    break;
                case 10:
                    handleGenerateOTP();
                    break;
                case 0:
                    currentUser = null;
                    System.out.println("✅ Logged out successfully!");
                    logger.info("User logged out");
                    break;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number.");
        }
        
        return true;
    }
    
    private static void handleAddEntry() {
        System.out.println("\n═══ Add Password Entry ═══");
        
        try {
            System.out.print("Account name (e.g., Gmail, Facebook): ");
            String accountName = scanner.nextLine().trim();
            
            System.out.print("Username/Email (optional): ");
            String username = scanner.nextLine().trim();
            
            System.out.print("Do you want to generate a random password? (y/n): ");
            String generate = scanner.nextLine().trim().toLowerCase();
            
            String password;
            if (generate.equals("y")) {
                password = PasswordGenerator.generate();
                System.out.println("Generated password: " + password);
            } else {
                System.out.print("Enter password: ");
                password = scanner.nextLine().trim();
            }
            
            System.out.print("Notes (optional): ");
            String notes = scanner.nextLine().trim();
            
            System.out.print("Enter your master password: ");
            String masterPassword = scanner.nextLine().trim();
            
            vaultService.addEntry(currentUser.getId(), accountName, username, 
                                 password, notes, masterPassword);
            
            System.out.println("\n✅ Password entry added successfully!");
            logger.info("Vault entry added by user: " + currentUser.getId());
            
        } catch (Exception e) {
            System.out.println("❌ Failed to add entry: " + e.getMessage());
            logger.error("Add entry error", e);
        }
    }
    
    private static void handleListEntries() {
        System.out.println("\n═══ Your Password Vault ═══");
        
        try {
            List<VaultEntry> entries = vaultService.listEntries(currentUser.getId());
            
            if (entries.isEmpty()) {
                System.out.println("Your vault is empty. Add some passwords to get started!");
                return;
            }
            
            System.out.println("\nTotal entries: " + entries.size());
            System.out.println("─────────────────────────────────────────");
            
            for (int i = 0; i < entries.size(); i++) {
                VaultEntry entry = entries.get(i);
                System.out.printf("%d. %s", i + 1, entry.getAccountName());
                if (entry.getUsername() != null && !entry.getUsername().isEmpty()) {
                    System.out.printf(" (%s)", entry.getUsername());
                }
                System.out.printf(" [Added: %s]%n", entry.getCreatedAt().toLocalDate());
            }
            
            logger.info("User " + currentUser.getId() + " listed vault entries");
            
        } catch (Exception e) {
            System.out.println("❌ Failed to list entries: " + e.getMessage());
            logger.error("List entries error", e);
        }
    }
    
    private static void handleViewEntry() {
        System.out.println("\n═══ View Password Details ═══");
        
        try {
            System.out.print("Enter account name: ");
            String accountName = scanner.nextLine().trim();
            
            System.out.print("Enter your master password: ");
            String masterPassword = scanner.nextLine().trim();
            
            VaultEntry entry = vaultService.viewEntry(currentUser.getId(), accountName, masterPassword);
            
            System.out.println("\n─── Password Details ───");
            System.out.println("Account: " + entry.getAccountName());
            System.out.println("Username: " + (entry.getUsername() != null ? entry.getUsername() : "N/A"));
            System.out.println("Password: " + entry.getPassword());
            System.out.println("Notes: " + (entry.getNotes() != null ? entry.getNotes() : "N/A"));
            System.out.println("Created: " + entry.getCreatedAt());
            System.out.println("Last Updated: " + entry.getUpdatedAt());
            
            logger.info("User " + currentUser.getId() + " viewed entry: " + accountName);
            
        } catch (Exception e) {
            System.out.println("❌ Failed to view entry: " + e.getMessage());
            logger.error("View entry error", e);
        }
    }
    
    private static void handleSearchEntries() {
        System.out.println("\n═══ Search Passwords ═══");
        
        try {
            System.out.print("Enter search term: ");
            String searchTerm = scanner.nextLine().trim();
            
            List<VaultEntry> entries = vaultService.searchEntries(currentUser.getId(), searchTerm);
            
            if (entries.isEmpty()) {
                System.out.println("No matching entries found.");
                return;
            }
            
            System.out.println("\nFound " + entries.size() + " matching entries:");
            System.out.println("─────────────────────────────────────────");
            
            for (int i = 0; i < entries.size(); i++) {
                VaultEntry entry = entries.get(i);
                System.out.printf("%d. %s", i + 1, entry.getAccountName());
                if (entry.getUsername() != null && !entry.getUsername().isEmpty()) {
                    System.out.printf(" (%s)", entry.getUsername());
                }
                System.out.println();
            }
            
            logger.info("User " + currentUser.getId() + " searched: " + searchTerm);
            
        } catch (Exception e) {
            System.out.println("❌ Search failed: " + e.getMessage());
            logger.error("Search error", e);
        }
    }
    
    private static void handleUpdateEntry() {
        System.out.println("\n═══ Update Password ═══");
        
        try {
            System.out.print("Enter account name: ");
            String accountName = scanner.nextLine().trim();
            
            System.out.print("Do you want to generate a new random password? (y/n): ");
            String generate = scanner.nextLine().trim().toLowerCase();
            
            String newPassword;
            if (generate.equals("y")) {
                newPassword = PasswordGenerator.generate();
                System.out.println("Generated password: " + newPassword);
            } else {
                System.out.print("Enter new password: ");
                newPassword = scanner.nextLine().trim();
            }
            
            System.out.print("Update notes (leave empty to keep current): ");
            String notes = scanner.nextLine().trim();
            
            System.out.print("Enter your master password: ");
            String masterPassword = scanner.nextLine().trim();
            
            vaultService.updateEntry(currentUser.getId(), accountName, newPassword, 
                                    notes.isEmpty() ? null : notes, masterPassword);
            
            System.out.println("\n✅ Password updated successfully!");
            logger.info("User " + currentUser.getId() + " updated entry: " + accountName);
            
        } catch (Exception e) {
            System.out.println("❌ Failed to update entry: " + e.getMessage());
            logger.error("Update entry error", e);
        }
    }
    
    private static void handleDeleteEntry() {
        System.out.println("\n═══ Delete Password Entry ═══");
        
        try {
            System.out.print("Enter account name: ");
            String accountName = scanner.nextLine().trim();
            
            System.out.print("Are you sure you want to delete this entry? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (!confirm.equals("yes")) {
                System.out.println("❌ Deletion cancelled.");
                return;
            }
            
            System.out.print("Enter your master password: ");
            String masterPassword = scanner.nextLine().trim();
            
            vaultService.deleteEntry(currentUser.getId(), accountName, masterPassword);
            
            System.out.println("\n✅ Password entry deleted successfully!");
            logger.info("User " + currentUser.getId() + " deleted entry: " + accountName);
            
        } catch (Exception e) {
            System.out.println("❌ Failed to delete entry: " + e.getMessage());
            logger.error("Delete entry error", e);
        }
    }
    
    private static void handleGeneratePassword() {
        System.out.println("\n═══ Generate Random Password ═══");
        
        try {
            System.out.print("Enter password length (default 16): ");
            String lengthStr = scanner.nextLine().trim();
            int length = lengthStr.isEmpty() ? 16 : Integer.parseInt(lengthStr);
            
            System.out.print("Include uppercase letters? (y/n, default y): ");
            String upperStr = scanner.nextLine().trim().toLowerCase();
            boolean includeUpper = upperStr.isEmpty() || upperStr.equals("y");
            
            System.out.print("Include numbers? (y/n, default y): ");
            String digitsStr = scanner.nextLine().trim().toLowerCase();
            boolean includeDigits = digitsStr.isEmpty() || digitsStr.equals("y");
            
            System.out.print("Include special characters? (y/n, default y): ");
            String specialStr = scanner.nextLine().trim().toLowerCase();
            boolean includeSpecial = specialStr.isEmpty() || specialStr.equals("y");
            
            String password = PasswordGenerator.generate(length, includeUpper, includeDigits, includeSpecial);
            int strength = PasswordGenerator.calculateStrength(password);
            
            System.out.println("\n─── Generated Password ───");
            System.out.println("Password: " + password);
            System.out.println("Length: " + password.length());
            System.out.println("Strength: " + strength + "/100");
            
            logger.info("User " + currentUser.getId() + " generated password");
            
        } catch (Exception e) {
            System.out.println("❌ Failed to generate password: " + e.getMessage());
            logger.error("Password generation error", e);
        }
    }
    
    private static void handleChangePassword() {
        System.out.println("\n═══ Change Login Password ═══");
        
        try {
            System.out.print("Enter current password: ");
            String oldPassword = scanner.nextLine().trim();
            
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine().trim();
            
            System.out.print("Confirm new password: ");
            String confirmPassword = scanner.nextLine().trim();
            
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("❌ Passwords do not match!");
                return;
            }
            
            authService.changePassword(currentUser.getId(), oldPassword, newPassword);
            
            System.out.println("\n✅ Password changed successfully!");
            logger.info("User " + currentUser.getId() + " changed password");
            
        } catch (Exception e) {
            System.out.println("❌ Failed to change password: " + e.getMessage());
            logger.error("Change password error", e);
        }
    }
    
    private static void handleUpdateProfile() {
        System.out.println("\n═══ Update Profile ═══");
        
        try {
            System.out.println("Current name: " + currentUser.getName());
            System.out.print("Enter new name (leave empty to keep current): ");
            String newName = scanner.nextLine().trim();
            
            System.out.println("Current email: " + currentUser.getEmail());
            System.out.print("Enter new email (leave empty to keep current): ");
            String newEmail = scanner.nextLine().trim();
            
            if (newName.isEmpty() && newEmail.isEmpty()) {
                System.out.println("❌ No changes made.");
                return;
            }
            
            authService.updateProfile(
                currentUser.getId(),
                newName.isEmpty() ? currentUser.getName() : newName,
                newEmail.isEmpty() ? currentUser.getEmail() : newEmail
            );
            
            // Refresh current user
            currentUser = authService.login(
                newEmail.isEmpty() ? currentUser.getEmail() : newEmail,
                "dummy"  // We need to fetch from DB differently
            );
            
            System.out.println("\n✅ Profile updated successfully!");
            logger.info("User " + currentUser.getId() + " updated profile");
            
        } catch (Exception e) {
            System.out.println("❌ Failed to update profile: " + e.getMessage());
            logger.error("Update profile error", e);
        }
    }
    
    private static void handleGenerateOTP() {
        System.out.println("\n═══ Generate Verification Code (OTP) ═══");
        
        try {
            String otp = authService.generateOTP(currentUser.getId());
            
            System.out.println("\n─── Your Verification Code ───");
            System.out.println("OTP: " + otp);
            System.out.println("Valid for: 5 minutes");
            System.out.println("This code can be used for sensitive operations.");
            
            System.out.print("\nDo you want to verify this OTP? (y/n): ");
            String verify = scanner.nextLine().trim().toLowerCase();
            
            if (verify.equals("y")) {
                System.out.print("Enter OTP code: ");
                String code = scanner.nextLine().trim();
                
                if (authService.verifyOTP(currentUser.getId(), code)) {
                    System.out.println("✅ OTP verified successfully!");
                } else {
                    System.out.println("❌ Invalid or expired OTP!");
                }
            }
            
            logger.info("User " + currentUser.getId() + " generated OTP");
            
        } catch (Exception e) {
            System.out.println("❌ Failed to generate OTP: " + e.getMessage());
            logger.error("OTP generation error", e);
        }
    }
}
