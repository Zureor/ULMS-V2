package com.ulms.repository;

import com.ulms.model.Book;
import com.ulms.model.Reservation;
import com.ulms.model.User;
import com.ulms.model.enums.ReservationStatus;
import com.ulms.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldSaveAndFindReservation() {
        User user = createTestUser("testuser", "Test User", "test@test.com", UserRole.STUDENT);
        Book book = createTestBook("978-100", "Test Book", "Author", "Programming");
        userRepository.save(user);
        bookRepository.save(book);

        Reservation r = new Reservation();
        r.setUser(user);
        r.setBook(book);
        r.setStatus(ReservationStatus.PENDING);
        Reservation saved = reservationRepository.save(r);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void shouldFindByUserIdAndStatus() {
        User user = createTestUser("user2", "User2", "u2@test.com", UserRole.STUDENT);
        Book book = createTestBook("978-101", "Book2", "Author2", "Programming");
        userRepository.save(user);
        bookRepository.save(book);

        Reservation r = new Reservation();
        r.setUser(user);
        r.setBook(book);
        r.setStatus(ReservationStatus.ISSUED);
        reservationRepository.save(r);

        List<Reservation> found = reservationRepository.findByUserIdAndStatus(user.getId(), ReservationStatus.ISSUED);
        assertThat(found).hasSize(1);
    }

    @Test
    void shouldCountActiveReservations() {
        User user = createTestUser("user3", "User3", "u3@test.com", UserRole.STUDENT);
        Book book = createTestBook("978-102", "Book3", "Author3", "Programming");
        userRepository.save(user);
        bookRepository.save(book);

        Reservation r1 = new Reservation();
        r1.setUser(user);
        r1.setBook(book);
        r1.setStatus(ReservationStatus.PENDING);

        Reservation r2 = new Reservation();
        r2.setUser(user);
        r2.setBook(book);
        r2.setStatus(ReservationStatus.ISSUED);

        reservationRepository.saveAll(List.of(r1, r2));

        long count = reservationRepository.countByUserIdAndStatus(user.getId(), ReservationStatus.PENDING);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldFindOverdueReservations() {
        User user = createTestUser("user4", "User4", "u4@test.com", UserRole.STUDENT);
        Book book = createTestBook("978-103", "Book4", "Author4", "Programming");
        userRepository.save(user);
        bookRepository.save(book);

        Reservation overdue = new Reservation();
        overdue.setUser(user);
        overdue.setBook(book);
        overdue.setStatus(ReservationStatus.ISSUED);
        overdue.setIssueDate(java.time.LocalDate.now().minusDays(20));
        overdue.setDueDate(java.time.LocalDate.now().minusDays(5));
        reservationRepository.save(overdue);

        List<Long> overdueIds = reservationRepository.findOverdueReservations(java.time.LocalDate.now());
        assertThat(overdueIds).contains(overdue.getId());
    }

    private User createTestUser(String username, String name, String email, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYn");
        user.setFullName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Book createTestBook(String isbn, String title, String author, String category) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
        return book;
    }
}