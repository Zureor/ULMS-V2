package com.ulms.repository;

import com.ulms.model.Book;
import com.ulms.model.Fine;
import com.ulms.model.Reservation;
import com.ulms.model.User;
import com.ulms.model.enums.FineStatus;
import com.ulms.model.enums.ReservationStatus;
import com.ulms.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FineRepositoryTest {

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void shouldFindUnpaidFinesByUser() {
        User user = createTestUser("ftest1", "Fine User", "f@test.com", "STUDENT");
        user = userRepository.save(user);
        Book book = bookRepository.save(createTestBook("978-200", "Fine Book", "Author", "Programming"));
        Reservation r = reservationRepository.save(createTestReservation(user, book, ReservationStatus.ISSUED));

        Fine fine = new Fine();
        fine.setUser(user);
        fine.setReservation(r);
        fine.setAmount(new BigDecimal("2.50"));
        fine.setReason("OVERDUE_5_DAYS");
        fine.setStatus(FineStatus.UNPAID);
        fineRepository.save(fine);

        List<Fine> fines = fineRepository.findByUserIdAndStatus(user.getId(), FineStatus.UNPAID);
        assertThat(fines).hasSize(1);
    }

    @Test
    void shouldCalculateTotalUnpaidFines() {
        User user = createTestUser("ftest2", "Fine User2", "f2@test.com", "STUDENT");
        user = userRepository.save(user);
        Book book = bookRepository.save(createTestBook("978-201", "Fine Book2", "Author", "Programming"));
        Reservation r = reservationRepository.save(createTestReservation(user, book, ReservationStatus.ISSUED));

        Fine fine1 = new Fine();
        fine1.setUser(user);
        fine1.setReservation(r);
        fine1.setAmount(new BigDecimal("2.50"));
        fine1.setReason("OVERDUE");
        fine1.setStatus(FineStatus.UNPAID);
        fineRepository.save(fine1);

        Fine fine2 = new Fine();
        fine2.setUser(user);
        fine2.setReservation(r);
        fine2.setAmount(new BigDecimal("1.50"));
        fine2.setReason("OVERDUE");
        fine2.setStatus(FineStatus.UNPAID);
        fineRepository.save(fine2);

        BigDecimal total = fineRepository.getTotalUnpaidFinesByUserId(user.getId());
        assertThat(total).isEqualByComparingTo("4.00");
    }

    private User createTestUser(String username, String name, String email, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYn");
        user.setFullName(name);
        user.setEmail(email);
        user.setRole(UserRole.valueOf(role));
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

    private Reservation createTestReservation(User user, Book book, ReservationStatus status) {
        Reservation r = new Reservation();
        r.setUser(user);
        r.setBook(book);
        r.setStatus(status);
        if (status == ReservationStatus.ISSUED) {
            r.setIssueDate(java.time.LocalDate.now().minusDays(10));
            r.setDueDate(java.time.LocalDate.now().minusDays(5));
        }
        return r;
    }
}