# ULMS-V2 (University Library Management System)

A Spring Boot 3 application with MySQL persistence and automatic fine calculation.

## 1. System Requirements
- **Java Runtime**: JDK 17
- **Build Tool**: Maven 3.6+
- **Database**: MySQL 8.0
- **Operating System**: Linux / Windows / macOS

## 2. Environmental Configuration
The application connects to MySQL using the following parameters defined in `src/main/resources/application.yml`:

| Parameter | Value |
| :--- | :--- |
| **Host** | `localhost` |
| **Port** | `3306` |
| **Database Name** | `ulms_db` |
| **Username** | `ulms_user` |
| **Password** | `ulms_pass` |

## 3. Database Initialization (Execute as Root)
Copy and run these SQL commands in your MySQL terminal to prepare the environment:

```sql
CREATE DATABASE IF NOT EXISTS ulms_db;
CREATE USER IF NOT EXISTS 'ulms_user'@'localhost' IDENTIFIED BY 'ulms_pass';
GRANT ALL PRIVILEGES ON ulms_db.* TO 'ulms_user'@'localhost';
FLUSH PRIVILEGES;
```

## 4. Build and Execution Sequence
Follow these commands in exact order from the project root directory:

1. **Clean and Install Dependencies**:
   ```bash
   mvn clean install -DskipTests
   ```

2. **Launch Application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Access Application**:
   - Web UI: http://localhost:8080
   - API Docs: http://localhost:8080/api-docs

## 5. Pre-seeded Test Credentials
| Role | Username | Password |
| :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` |
| **Librarian** | `librarian1` | `lib123` |
| **Student** | `student1` | `student123` |

## 6. Logic Verification (Auto-Fines)
1. Login as `librarian1`.
2. Go to **Reservations**.
3. Locate the overdue record for `student1`.
4. Click **Return**.
5. The system will automatically calculate the fine ($0.50 per day) and update the `fines` table.
