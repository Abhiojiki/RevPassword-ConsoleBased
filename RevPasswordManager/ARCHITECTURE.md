# Application Architecture

## Architecture Overview

RevPassword Manager follows a **Layered Architecture** pattern with clear separation of concerns. This architecture promotes maintainability, testability, and scalability.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                     Main.java                            │  │
│  │  • Console-based User Interface                          │  │
│  │  • User Input/Output Handling                            │  │
│  │  • Menu Navigation                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                              │
│                                                                 │
│  ┌──────────────────────┐        ┌───────────────────────────┐ │
│  │   AuthService        │        │    VaultService           │ │
│  │                      │        │                           │ │
│  │ • User Signup        │        │ • Add Password Entry      │ │
│  │ • User Login         │        │ • List Passwords          │ │
│  │ • Password Reset     │        │ • View Password Details   │ │
│  │ • OTP Generation     │        │ • Update Passwords        │ │
│  │ • Profile Update     │        │ • Delete Passwords        │ │
│  └──────────────────────┘        │ • Search Passwords        │ │
│                                  └───────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER (DAO)                        │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐ │
│  │ UserRepo     │  │ VaultRepo    │  │    OTPRepo           │ │
│  │              │  │              │  │                      │ │
│  │ • save()     │  │ • save()     │  │ • save()             │ │
│  │ • findById() │  │ • findByUser │  │ • findByUserId()     │ │
│  │ • findByEmail│  │ • search()   │  │ • delete()           │ │
│  │ • update()   │  │ • update()   │  │ • cleanup()          │ │
│  │ • delete()   │  │ • delete()   │  │                      │ │
│  └──────────────┘  └──────────────┘  └──────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                    UTILITY LAYER                                │
│                                                                 │
│  ┌────────────────────┐  ┌──────────────────────────────────┐  │
│  │ EncryptionUtil     │  │   PasswordGenerator              │  │
│  │                    │  │                                  │  │
│  │ • hashPassword()   │  │ • generate()                     │  │
│  │ • verifyPassword() │  │ • calculateStrength()            │  │
│  │ • encrypt()        │  │                                  │  │
│  │ • decrypt()        │  │                                  │  │
│  └────────────────────┘  └──────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                   CONFIGURATION LAYER                           │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │          DatabaseConfig                                    │ │
│  │                                                            │ │
│  │  • Database Connection Management                          │ │
│  │  • Connection Pool                                         │ │
│  │  • Schema Initialization                                   │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                               │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    MySQL Database                         │  │
│  │                                                           │  │
│  │  ┌─────────┐  ┌──────────────┐  ┌──────────────────┐   │  │
│  │  │  Users  │  │ Vault_Entries│  │       OTP        │   │  │
│  │  └─────────┘  └──────────────┘  └──────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Layer Descriptions

### 1. Presentation Layer (UI)

**Location**: `com.revature.passwordmanager.Main`

**Responsibilities**:
- Console-based user interface
- Capturing user input
- Displaying output to users
- Menu navigation
- Input validation (basic)

**Key Classes**:
- `Main.java`: Entry point and UI controller

**Technologies**:
- Java Scanner for input
- System.out for output

---

### 2. Service Layer (Business Logic)

**Location**: `com.revature.passwordmanager.service`

**Responsibilities**:
- Business logic implementation
- Data validation
- Transaction orchestration
- Exception handling
- Logging

**Key Classes**:

#### AuthService
- User registration and authentication
- Password management
- Security question handling
- OTP generation and verification
- Profile updates

#### VaultService
- Password vault operations
- Master password verification
- Encryption/decryption orchestration
- Search functionality

**Technologies**:
- Log4J 2 for logging
- BCrypt for password hashing
- Custom business logic

---

### 3. Repository Layer (Data Access)

**Location**: `com.revature.passwordmanager.repository`

**Responsibilities**:
- Database CRUD operations
- SQL query execution
- Result set mapping to objects
- Exception handling for SQL errors

**Key Classes**:

#### UserRepository
- User CRUD operations
- Find by email/ID
- User updates

#### VaultRepository
- Vault entry CRUD operations
- Search by account name
- User-specific queries

#### OTPRepository
- OTP storage and retrieval
- Expiry management
- Cleanup of old OTPs

**Technologies**:
- JDBC for database connectivity
- PreparedStatement for SQL injection prevention
- Connection pooling (via DriverManager)

---

### 4. Utility Layer

**Location**: `com.revature.passwordmanager.util`

**Responsibilities**:
- Cross-cutting concerns
- Reusable utilities
- Encryption/decryption
- Password generation

**Key Classes**:

#### EncryptionUtil
- BCrypt password hashing
- AES encryption/decryption
- Key generation

#### PasswordGenerator
- Random password generation
- Password strength calculation
- Customizable character sets

**Technologies**:
- BCrypt (jBCrypt library)
- Java Crypto API (AES)
- SecureRandom for randomness

---

### 5. Configuration Layer

**Location**: `com.revature.passwordmanager.config`

**Responsibilities**:
- Application configuration
- Database connection management
- Property file loading
- Schema initialization

**Key Classes**:

#### DatabaseConfig
- Connection factory
- Database initialization
- Configuration loading

**Technologies**:
- Properties files
- JDBC DriverManager

---

### 6. Model Layer (Domain Objects)

**Location**: `com.revature.passwordmanager.model`

**Responsibilities**:
- Represent domain entities
- Data encapsulation
- Validation (basic)

**Key Classes**:
- `User`: User account information
- `VaultEntry`: Password vault entries
- `OTP`: One-time passwords

---

## Design Patterns Used

### 1. Layered Architecture Pattern
- Clear separation of concerns
- Each layer has specific responsibilities
- Dependencies flow downward

### 2. Repository Pattern
- Abstraction of data access logic
- Centralized data access
- Easier to test and maintain

### 3. Service Layer Pattern
- Business logic encapsulation
- Transaction management
- Orchestration of operations

### 4. Singleton Pattern
- DatabaseConfig uses static methods
- Single configuration instance

### 5. Factory Pattern
- Connection creation in DatabaseConfig
- Object creation in repositories

---

## Data Flow Examples

### Example 1: User Login

```
1. User enters email and password in Main.java
                    ↓
2. Main.java calls AuthService.login(email, password)
                    ↓
3. AuthService calls UserRepository.findByEmail(email)
                    ↓
4. UserRepository executes SQL query via JDBC
                    ↓
5. Database returns user data
                    ↓
6. UserRepository maps ResultSet to User object
                    ↓
7. AuthService verifies password using EncryptionUtil
                    ↓
8. AuthService returns User object to Main.java
                    ↓
9. Main.java displays success message
```

### Example 2: Adding a Password Entry

```
1. User provides account details and master password
                    ↓
2. Main.java calls VaultService.addEntry(...)
                    ↓
3. VaultService verifies master password
   • Calls UserRepository.findById()
   • Calls EncryptionUtil.verifyPassword()
                    ↓
4. VaultService encrypts password
   • Calls EncryptionUtil.encryptVaultPassword()
                    ↓
5. VaultService calls VaultRepository.save()
                    ↓
6. VaultRepository inserts encrypted data into database
                    ↓
7. Success message propagates back to Main.java
```

---

## Security Architecture

### Authentication Flow
```
User Credentials → BCrypt Hash → Database Storage
                                      ↓
Login Attempt → BCrypt Verify ← Database Retrieval
```

### Vault Encryption Flow
```
Plain Password → AES Encrypt (using Master Password) → Database
                                                             ↓
Database → AES Decrypt (using Master Password) → Plain Password
```

### Security Layers
1. **Transport Layer**: Console (local application)
2. **Application Layer**: 
   - Input validation
   - Master password verification
3. **Data Layer**: 
   - Password hashing (BCrypt)
   - Password encryption (AES)
   - Parameterized queries

---

## Technology Stack

### Core Technologies
- **Language**: Java 11
- **Build Tool**: Maven 3.6+
- **Database**: MySQL 8.0

### Dependencies
```xml
<!-- Database -->
mysql-connector-java 8.0.33

<!-- Logging -->
log4j-core 2.20.0
log4j-api 2.20.0

<!-- Testing -->
junit-jupiter 5.10.0
mockito-core 5.5.0

<!-- Security -->
jbcrypt 0.4
```

---

## Deployment Architecture

```
┌────────────────────────────────────────┐
│       Development Environment          │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │  Java Application (JAR)          │ │
│  │  • RevPasswordManager            │ │
│  └──────────────────────────────────┘ │
│                ↓                       │
│  ┌──────────────────────────────────┐ │
│  │  MySQL Database Server           │ │
│  │  • localhost:3306                │ │
│  │  • revpassword_db                │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │  Log Files                       │ │
│  │  • logs/revpassword.log          │ │
│  └──────────────────────────────────┘ │
└────────────────────────────────────────┘
```

---

## Scalability Considerations

### Current Architecture Supports:
- ✅ Single user application
- ✅ Local database
- ✅ Console interface

### Future Enhancements:
- 🔄 Multi-tier web application
- 🔄 REST API layer
- 🔄 Connection pooling
- 🔄 Caching layer
- 🔄 Microservices architecture
- 🔄 Cloud deployment

---

## Testing Strategy

### Unit Testing
- Service layer: Business logic validation
- Utility layer: Encryption and generation logic
- Repository layer: Data access patterns (with mocks)

### Integration Testing
- Database operations
- End-to-end workflows

### Test Coverage Target
- Service Layer: 80%+
- Utility Layer: 90%+
- Repository Layer: 70%+

---

## Error Handling Strategy

### Exception Flow
```
Presentation Layer
    ↓ try-catch → Display user-friendly message
Service Layer
    ↓ throws Exception → Log error
Repository Layer
    ↓ throws SQLException → Wrap in custom exception
Database Layer
    → SQL Exception
```

### Logging Levels
- **ERROR**: Database errors, encryption failures
- **WARN**: Failed login attempts, invalid operations
- **INFO**: Successful operations, user actions
- **DEBUG**: Method entry/exit, SQL queries

---

## Configuration Management

### Property Files
- `database.properties`: Database connection settings
- `log4j2.xml`: Logging configuration

### Environment-Specific Configuration
- Development: Local MySQL
- Testing: In-memory or test database
- Production: Remote MySQL server

---

## Performance Considerations

### Optimizations Implemented:
1. **Prepared Statements**: Prevents SQL injection and improves performance
2. **Password Hashing**: BCrypt with optimal work factor (12)
3. **Connection Management**: Proper resource cleanup

### Future Optimizations:
- Connection pooling (HikariCP)
- Caching frequently accessed data
- Batch operations for multiple inserts
- Database indexing optimization

---

## Maintenance and Monitoring

### Logging
- All operations logged with Log4J
- Separate log files for different concerns
- Log rotation (10MB per file, max 10 files)

### Monitoring Points
- Failed login attempts
- Database connection failures
- Encryption/decryption errors
- OTP generation and verification

---

## Conclusion

This architecture provides:
- ✅ **Modularity**: Clear separation of concerns
- ✅ **Maintainability**: Easy to update individual layers
- ✅ **Testability**: Each layer can be tested independently
- ✅ **Scalability**: Foundation for future enhancements
- ✅ **Security**: Multiple layers of protection
