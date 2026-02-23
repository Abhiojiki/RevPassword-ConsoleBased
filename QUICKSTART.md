# 🚀 Quick Start Guide - RevPassword Manager

## 📥 Getting Started in 5 Minutes

### Step 1: Prerequisites Check ✓

Before you begin, make sure you have:
- [ ] Java 11 or higher
- [ ] IntelliJ IDEA (or any Java IDE)
- [ ] MySQL 8.0 or higher
- [ ] Maven 3.6+ (usually bundled with IntelliJ)

### Step 2: MySQL Setup (2 minutes)

1. **Start MySQL Server:**
   ```bash
   # Windows (Command Prompt as Administrator)
   net start MySQL80
   
   # macOS
   sudo mysql.server start
   
   # Linux
   sudo systemctl start mysql
   ```

2. **Optional: Create Database Manually**
   ```bash
   mysql -u root -p
   CREATE DATABASE revpassword_db;
   exit;
   ```
   *Note: Application can auto-create the database*

### Step 3: Import Project in IntelliJ (1 minute)

1. **Open IntelliJ IDEA**
2. Click **File** → **Open**
3. Select the `RevPasswordManager` folder
4. Click **OK**
5. Click **Trust Project** when prompted
6. Wait for Maven to download dependencies (watch progress bar)

### Step 4: Configure Database (30 seconds)

1. Open: `src/main/resources/database.properties`
2. Update these lines:
   ```properties
   db.username=root          # Your MySQL username
   db.password=your_password # Your MySQL password
   ```
3. Save the file (Ctrl+S)

### Step 5: Run the Application! (30 seconds)

1. Navigate to: `src/main/java/com/revature/passwordmanager/Main.java`
2. Right-click → **Run 'Main.main()'**
3. You should see:
   ```
   ╔══════════════════════════════════════════╗
   ║   RevPassword Manager Application       ║
   ║   Secure Password Management Solution    ║
   ╚══════════════════════════════════════════╝
   ```

**🎉 You're ready to go!**

---

## 🎯 First Time User Flow

### 1. Create Your Account
```
Select option: 1
Enter your name: John Doe
Enter email: john@example.com
Enter login password: MyPass123!
Confirm login password: MyPass123!
Enter master password: MasterPass456!
Confirm master password: MasterPass456!
Enter security question: What is your pet's name?
Enter security answer: Fluffy

✅ Account created successfully!
```

### 2. Login
```
Select option: 2
Enter email: john@example.com
Enter password: MyPass123!

✅ Login successful!
Welcome back, John Doe!
```

### 3. Add Your First Password
```
Select option: 1
Account name: Gmail
Username/Email: john@gmail.com
Do you want to generate a random password? (y/n): y
Generated password: aB3#xY9!mK2$pL5@
Notes: Personal Gmail account
Enter your master password: MasterPass456!

✅ Password entry added successfully!
```

### 4. View Your Passwords
```
Select option: 2

Total entries: 1
─────────────────────────────────────────
1. Gmail (john@gmail.com) [Added: 2025-02-08]
```

---

## 🛠️ Quick Troubleshooting

### Problem: "Database connection failed"
**Solution:**
1. Check if MySQL is running
2. Verify credentials in `database.properties`
3. Test connection: `mysql -u root -p`

### Problem: "Maven dependencies not downloading"
**Solution:**
1. Check internet connection
2. In IntelliJ: Right-click `pom.xml` → **Maven** → **Reload Project**

### Problem: "Cannot find main class"
**Solution:**
1. **Build** → **Rebuild Project**
2. Wait for build to complete

---

## 📚 Key Features to Try

### 🔐 Security Features
- [x] Password encryption
- [x] Master password protection
- [x] Security questions for recovery
- [x] OTP generation

### 🔑 Password Management
- [x] Add passwords
- [x] View passwords (with master password)
- [x] Update passwords
- [x] Delete passwords
- [x] Search passwords

### 🎲 Password Generation
- [x] Random password generation
- [x] Customizable length
- [x] Character type selection
- [x] Password strength calculator

---

## 🧪 Running Tests

Verify everything works:
```
Right-click on src/test/java → Run 'All Tests'
```

Expected result: All tests pass ✅

---

## 📖 More Information

- **Full documentation**: See `README.md`
- **Architecture details**: See `ARCHITECTURE.md`
- **Database schema**: See `ERD.md`
- **IntelliJ setup**: See `INTELLIJ_SETUP.md`

---

## 🆘 Need Help?

1. Check the `logs/` folder for error details
2. Review the documentation files
3. Verify MySQL is running and accessible

---

## ⚡ Pro Tips

1. **Use Strong Passwords**: Generate passwords with 16+ characters
2. **Unique Master Password**: Make it different from your login password
3. **Backup**: Export your vault regularly (feature coming soon)
4. **Security Questions**: Choose questions only you know the answer to

---

## 🎓 Learning Path

1. ✅ Run the application
2. ✅ Create an account
3. ✅ Add a few password entries
4. ✅ Try password generation
5. ✅ Test password recovery
6. 📝 Explore the code
7. 🧪 Run the tests
8. 🔧 Make customizations

---

**Happy Password Managing! 🔐**
