package com.ulms.util;

import com.ulms.model.Book;
import com.ulms.model.User;
import com.ulms.model.enums.UserRole;
import com.ulms.model.enums.ReservationStatus;
import com.ulms.model.enums.FineStatus;
import com.ulms.model.Reservation;
import com.ulms.model.Fine;
import com.ulms.repository.BookRepository;
import com.ulms.repository.UserRepository;
import com.ulms.repository.ReservationRepository;
import com.ulms.repository.FineRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UserRepository userRepository,
                         BookRepository bookRepository,
                         ReservationRepository reservationRepository,
                         FineRepository fineRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.reservationRepository = reservationRepository;
        this.fineRepository = fineRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
            seedBooks();
            seedActivity();
            System.out.println("Dev data seeded successfully!");
        }
    }

    private void seedUsers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("System Admin");
        admin.setEmail("admin@ulms.com");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        User librarian = new User();
        librarian.setUsername("librarian1");
        librarian.setPassword(passwordEncoder.encode("lib123"));
        librarian.setFullName("Jane Librarian");
        librarian.setEmail("librarian@ulms.com");
        librarian.setRole(UserRole.LIBRARIAN);
        librarian.setActive(true);
        userRepository.save(librarian);

        User student = new User();
        student.setUsername("student1");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setFullName("John Student");
        student.setEmail("student@ulms.com");
        student.setRole(UserRole.STUDENT);
        student.setStudentId("STU001");
        student.setDepartment("Computer Science");
        student.setActive(true);
        userRepository.save(student);
    }

    private void seedBooks() {
        createBook("978-0134685991", "Effective Java", "Joshua Bloch", "Programming", 5);
        createBook("978-0596009205", "Head First Design Patterns", "Eric Freeman", "Programming", 3);
        createBook("978-0061120084", "To Kill a Mockingbird", "Harper Lee", "Literature", 2);
    }

    private void seedActivity() {
        User student = userRepository.findByUsername("student1").orElse(null);
        java.util.List<Book> books = bookRepository.findAll();

        if (student != null && !books.isEmpty()) {
            Book book1 = books.get(0);
            Book book2 = books.size() > 1 ? books.get(1) : book1;

            // Seed a pending reservation
            Reservation r1 = new Reservation();
            r1.setUser(student);
            r1.setBook(book1);
            r1.setStatus(ReservationStatus.PENDING);
            reservationRepository.save(r1);
            
            // Hold the copy
            book1.setAvailableCopies(book1.getAvailableCopies() - 1);
            bookRepository.save(book1);

            // Seed an issued reservation (overdue to test auto-fine)
            Reservation r2 = new Reservation();
            r2.setUser(student);
            r2.setBook(book2);
            r2.setStatus(ReservationStatus.ISSUED);
            r2.setIssueDate(LocalDate.now().minusDays(20));
            r2.setDueDate(LocalDate.now().minusDays(6)); // 6 days overdue
            reservationRepository.save(r2);
            
            // Hold the copy
            book2.setAvailableCopies(book2.getAvailableCopies() - 1);
            bookRepository.save(book2);
        }
    }

    private void createBook(String isbn, String title, String author, String category, int copies) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        bookRepository.save(book);
    }
}
