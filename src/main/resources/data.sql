-- ============================================
-- ULMS Seed Data (Idempotent)
-- ============================================

-- Admin user: admin / admin123
-- Librarian: librarian1 / lib123
-- Student: student1 / student123

INSERT INTO `users` (`id`, `username`, `password`, `full_name`, `email`, `role`, `student_id`, `department`, `is_active`) VALUES
(1, 'admin', '$2a$12$bW4YXYZ9zK7pVcDVlEQM7e2geUUKkXwP83i64WQpCc7Drb5raoSEu', 'System Admin', 'admin@ulms.com', 'ADMIN', NULL, NULL, TRUE),
(2, 'librarian1', '$2a$12$SijyY5Ywna0iejfSwDVGCeIjU8kxzYmUS.ELCGVDjwjQm64uX5zwK', 'Jane Librarian', 'librarian@ulms.com', 'LIBRARIAN', NULL, NULL, TRUE),
(3, 'student1', '$2a$12$6NKTsja18TLlYoRWwSpTLePlzPhiGA7XL8DJO4jJM/orDXkLCuIPC', 'John Student', 'student@ulms.com', 'STUDENT', 'STU001', 'Computer Science', TRUE)
ON DUPLICATE KEY UPDATE 
password = VALUES(password),
full_name = VALUES(full_name),
email = VALUES(email),
role = VALUES(role),
is_active = VALUES(is_active);

INSERT IGNORE INTO `books` (`id`, `isbn`, `title`, `author`, `publisher`, `edition`, `category`, `total_copies`, `available_copies`, `description`, `added_by`) VALUES
(1, '978-0134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', '3rd', 'Programming', 5, 5, 'Best practices for Java programming', 1),
(2, '978-0596009205', 'Head First Design Patterns', 'Eric Freeman', 'O''Reilly', '1st', 'Programming', 3, 3, 'A brain-friendly guide to design patterns', 1),
(3, '978-0131103627', 'The C Programming Language', 'Dennis Ritchie', 'Prentice Hall', '2nd', 'Programming', 4, 4, 'Classic C programming book', 1),
(4, '978-0061120084', 'To Kill a Mockingbird', 'Harper Lee', 'Harper Perennial', '50th', 'Literature', 2, 2, 'Classic American novel', 1),
(5, '978-0316769488', 'The Catcher in the Rye', 'J.D. Salinger', 'Little, Brown', '1st', 'Literature', 3, 3, 'Classic coming-of-age novel', 1);