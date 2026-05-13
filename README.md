# ULMS - University Library Management System

ULMS is a modern, Spring Boot-based Library Management System designed to handle book reservations, fine management, and user roles (Admin, Librarian, Student).

## Features

-   **Automatic Fine Calculation:** Fines are automatically generated when books are returned after the due date.
-   **Role-Based Access Control:** Distinct dashboards for Admin, Librarian, and Students.
-   **Book Management:** Track total vs. available copies.
-   **Reservation System:** Request, approve, and return workflow.
-   **Audit Logging:** Track important system actions.
-   **Persistent Storage:** Uses MySQL for data durability.

---

## Prerequisites

Before setting up the project, ensure you have the following installed:

-   **Java 17 or higher** (OpenJDK recommended)
-   **Maven 3.6+**
-   **MySQL Server 8.0+**
-   **Git**

---

## Getting Started

### 1. Clone the Repository
```bash
git clone <your-repository-url>
cd ulms-springboot
```

### 2. Database Setup
1.  Log in to your MySQL terminal:
    ```sql
    mysql -u root -p
    ```
2.  Run the following commands to create the database and user:
    ```sql
    CREATE DATABASE IF NOT EXISTS ulms_db;
    CREATE USER 'ulms_user'@'localhost' IDENTIFIED BY 'ulms_pass';
    GRANT ALL PRIVILEGES ON ulms_db.* TO 'ulms_user'@'localhost';
    FLUSH PRIVILEGES;
    ```

### 3. Configuration
The application is pre-configured to connect to `ulms_db` via `ulms_user`. You can modify these settings in:
`src/main/resources/application.yml`

### 4. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```
The application will be accessible at: **http://localhost:8080**

---

## Default Credentials (Initial Seed Data)

| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` |
| **Librarian** | `librarian1` | `lib123` |
| **Student** | `student1` | `student123` |

---

## API Documentation
Once the app is running, you can access the Swagger/OpenAPI documentation at:
**http://localhost:8080/api-docs**

---

## Running Tests
To run the automated test suite (including the automatic fine calculation tests):
```bash
mvn test
```

## License
This project is open-source. Feel free to use and modify.
