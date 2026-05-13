# University Library Management System (ULMS)

Welcome to ULMS! This is a simple guide to help you get this library system running on your computer from scratch.

## 🌟 What is this?
This is a website for a university library where:
- Students can reserve books.
- Librarians can approve and return books.
- **Automatic Fines:** If a student returns a book late, the system calculates the fine automatically!

---

## 🛠️ Step 1: Install the Tools
Before you start, you need to download and install these three things:
1.  **Java (JDK 17):** [Download from here](https://adoptium.net/temurin/releases/?version=17)
2.  **MySQL Database:** [Download MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
3.  **Maven:** [Download from here](https://maven.apache.org/download.cgi)

---

## 🗄️ Step 2: Setup the Database
The system needs a place to store data. Open your **MySQL Workbench** or **MySQL Command Line** and run these commands:

```sql
CREATE DATABASE ulms_db;
CREATE USER 'ulms_user'@'localhost' IDENTIFIED BY 'ulms_pass';
GRANT ALL PRIVILEGES ON ulms_db.* TO 'ulms_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 🚀 Step 3: Run the Project
1.  Open your terminal or command prompt.
2.  Go to the project folder.
3.  Run this command to build the project:
    ```bash
    mvn clean install
    ```
4.  Run this command to start the website:
    ```bash
    mvn spring-boot:run
    ```

---

## 💻 Step 4: Open the Website
Once it says "Started UlmsApplication", open your web browser and go to:
**[http://localhost:8080](http://localhost:8080)**

### Try logging in!
| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` |
| **Librarian** | `librarian1` | `lib123` |
| **Student** | `student1` | `student123` |

---

## 🎯 How to test the "Auto-Fine" feature
1.  Log in as **librarian1**.
2.  Go to the **Reservations** menu.
3.  You will see a book that is already overdue.
4.  Click **Return**.
5.  Check the **Fines** menu—you'll see the system automatically added a fine for the late return!

---

## 📄 Developers
For automated AI setup instructions, see `PROMPT.md`.
