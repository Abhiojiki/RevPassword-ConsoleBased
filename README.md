# RevPassword Manager Application

## 📋 Project Overview

RevPassword Manager is a secure, console-based password management application that allows users to safely store and manage passwords for their various online accounts. The application emphasizes security through password encryption, BCrypt hashing, and master password protection.

## 🎯 Features

### Core Functionality
- ✅ **User Account Management**
  - Create account with master password
  - Secure login authentication
  - Password recovery via security questions
  - Profile update capabilities
  
- ✅ **Password Vault Management**
  - Add new password entries
  - List all stored passwords
  - View individual password details (with master password verification)
  - Update existing passwords
  - Delete password entries
  - Search passwords by account name
  
- ✅ **Security Features**
  - BCrypt password hashing for user credentials
  - AES encryption for vault passwords using master password as key
  - Master password protection for sensitive vault operations
  - OTP (One-Time Password) generation for verification
  - Security questions for account recovery
  
- ✅ **Password Generation**
  - Generate strong random passwords
  - Customizable length and character types
  - Password strength calculator

## 🏗️ Architecture

The application follows a **layered architecture** pattern:

```
┌─────────────────────────────────────┐
│      Presentation Layer (CLI)       │
│          Main.java                  │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│        Service Layer                │
│   AuthService, VaultService         │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│      Repository Layer (DAO)         │
│  UserRepository, VaultRepository    │
│        OTPRepository                │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│      Database Layer (MySQL)         │
│   Users, Vault Entries, OTP         │
└─────────────────────────────────────┘
```

### Layer Responsibilities

1. **Presentation Layer**: Console-based UI, user input/output
2. **Service Layer**: Business logic, validation, orchestration
3. **Repository Layer**: Database operations using JDBC
4. **Utility Layer**: Encryption, password generation, logging

## 🛠️ Technologies Used

- **Java 11**: Core programming language
- **Maven**: Build automation and dependency management
- **MySQL 8.0**: Relational database
- **JDBC**: Database connectivity
- **Log4J 2**: Logging framework
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for tests
- **BCrypt**: Password hashing
- **AES**: Symmetric encryption for vault passwords

## 📁 Project Structure

```
RevPasswordManager/
├── src/
│   ├── main/
│   │   ├── java/com/revature/passwordmanager/
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── VaultEntry.java
│   │   │   │   └── OTP.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── VaultRepository.java
│   │   │   │   └── OTPRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   └── VaultService.java
│   │   │   ├── util/
│   │   │   │   ├── EncryptionUtil.java
│   │   │   │   └── PasswordGenerator.java
│   │   │   └── Main.java
│   │   └── resources/
│   │       ├── database.properties
│   │       └── log4j2.xml
│   └── test/
│       └── java/com/revature/passwordmanager/
│           ├── service/
│           │   ├── AuthServiceTest.java
│           │   └── VaultServiceTest.java
│           └── util/
│               └── PasswordGeneratorTest.java
├── pom.xml
├── README.md
├── ERD.md
└── ARCHITECTURE.md
```

## 🚀 Setup Instructions

### Prerequisites

1. **Java Development Kit (JDK) 11 or higher**
   - Download from [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or [OpenJDK](https://openjdk.org/)
   - Verify installation: `java -version`

2. **Apache Maven 3.6+**
   - Download from [Maven](https://maven.apache.org/download.cgi)
   - Verify installation: `mvn -version`

3. **MySQL 8.0+**
   - Download from [MySQL](https://dev.mysql.com/downloads/)
   - Start MySQL service

4. **IntelliJ IDEA** (recommended) or any Java IDE
   - Download from [JetBrains](https://www.jetbrains.com/idea/)

### Database Setup

1. **Start MySQL Server**
   ```bash
   # On Windows (if installed as service)
   net start MySQL80
   
   # On macOS/Linux
   sudo systemctl start mysql
   # or
   sudo service mysql start
   ```

2. **Create Database** (Optional - application can create it automatically)
   ```sql
   CREATE DATABASE revpassword_db;
   ```

3. **Configure Database Connection**
   - Open `src/main/resources/database.properties`
   - Update the following properties:
     ```properties
     db.url=jdbc:mysql://localhost:3306/revpassword_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
     db.username=root
     db.password=your_mysql_password
     ```

### Installation Steps

#### Option 1: Using IntelliJ IDEA

1. **Import Project**
   - Open IntelliJ IDEA
   - Select `File` → `Open`
   - Navigate to the `RevPasswordManager` folder
   - Click `OK`
   - IntelliJ will automatically detect it as a Maven project

2. **Configure Maven**
   - IntelliJ usually auto-imports Maven dependencies
   - If not, right-click on `pom.xml` → `Maven` → `Reload Project`

3. **Update Database Configuration**
   - Edit `src/main/resources/database.properties` with your MySQL credentials

4. **Build Project**
   - Click `Build` → `Build Project` (Ctrl+F9)
   - Or run: `mvn clean install`

5. **Run Application**
   - Navigate to `src/main/java/com/revature/passwordmanager/Main.java`
   - Right-click → `Run 'Main.main()'`
   - Or use the green play button next to the main method

#### Option 2: Using Command Line

1. **Extract Project**
   ```bash
   unzip RevPasswordManager.zip
   cd RevPasswordManager
   ```

2. **Update Database Configuration**
   ```bash
   # Edit the database.properties file
   nano src/main/resources/database.properties
   ```

3. **Build Project**
   ```bash
   mvn clean install
   ```

4. **Run Application**
   ```bash
   mvn exec:java -Dexec.mainClass="com.revature.passwordmanager.Main"
   ```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run tests with coverage
mvn clean test jacoco:report
```

## 📊 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    master_password VARCHAR(255) NOT NULL,
    security_question VARCHAR(255) NOT NULL,
    security_answer VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Vault Entries Table
```sql
CREATE TABLE vault_entries (
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
);
```

### OTP Table
```sql
CREATE TABLE otp (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    code VARCHAR(10) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## 🔒 Security Features

1. **Password Hashing**: User passwords and security answers are hashed using BCrypt (12 rounds)
2. **Vault Encryption**: Stored passwords are encrypted using AES-256 with master password as key
3. **Master Password**: Additional layer of protection for viewing/modifying vault entries
4. **OTP Verification**: Time-limited one-time passwords for sensitive operations
5. **Security Questions**: Account recovery mechanism
6. **SQL Injection Prevention**: Parameterized queries throughout

## 📝 Usage Examples

### Creating an Account
```
1. Select "Create Account"
2. Enter name, email, password
3. Set master password (for vault access)
4. Set security question and answer
```

### Adding a Password Entry
```
1. Login to your account
2. Select "Add Password Entry"
3. Enter account name (e.g., "Gmail")
4. Choose to generate or enter password
5. Enter master password to confirm
```

### Viewing a Password
```
1. Select "View Password Details"
2. Enter account name
3. Enter master password to decrypt
4. Password will be displayed
```

## 🧪 Testing

The project includes comprehensive unit tests:

- **AuthServiceTest**: Tests for authentication operations
- **VaultServiceTest**: Tests for vault management
- **PasswordGeneratorTest**: Tests for password generation

Run tests with: `mvn test`

## 📈 Logging

Application logs are stored in:
- `logs/revpassword.log`: All logs
- `logs/revpassword-rolling.log`: Rolling logs (max 10MB per file)

Log levels can be configured in `src/main/resources/log4j2.xml`

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running
   - Check credentials in `database.properties`
   - Ensure database exists or auto-create is enabled

2. **Maven Dependencies Not Found**
   - Run `mvn clean install -U` to force update
   - Check internet connection
   - Verify Maven settings

3. **ClassNotFoundException for MySQL Driver**
   - Ensure MySQL dependency is in `pom.xml`
   - Run `mvn clean install`

4. **Port Already in Use**
   - Check if another MySQL instance is running
   - Change port in database.properties if needed

## 👥 Contributors

- Your Name - RevPassword Manager Development


## 🔮 Future Enhancements

- Web-based UI
- Multi-factor authentication
- Password sharing features
- Password strength analysis
- Import/export functionality
- Browser extensions
