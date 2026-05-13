package com.ulms.service;

import com.ulms.dto.request.ReservationRequest;
import com.ulms.model.Book;
import com.ulms.model.Reservation;
import com.ulms.model.User;
import com.ulms.model.enums.ReservationStatus;
import com.ulms.model.enums.UserRole;
import com.ulms.repository.BookRepository;
import com.ulms.repository.ReservationRepository;
import com.ulms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private com.ulms.repository.FineRepository fineRepository;

    private Long studentId;
    private Long bookId;

    @BeforeEach
    void setUp() {
        User student = new User();
        student.setUsername("reserve_student");
        student.setPassword("encoded");
        student.setFullName("Reserve Student");
        student.setEmail("rs@test.com");
        student.setRole(UserRole.STUDENT);
        student.setStudentId("STU999");
        student.setActive(true);
        student = userRepository.save(student);
        studentId = student.getId();

        Book book = new Book();
        book.setIsbn("978-RESERVE");
        book.setTitle("Reserve Book");
        book.setAuthor("Author");
        book.setCategory("Programming");
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
        book = bookRepository.save(book);
        bookId = book.getId();
    }

    @Test
    void shouldCreateReservation() {
        Reservation r = reservationService.createReservation(studentId, bookId);

        assertThat(r.getId()).isNotNull();
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void shouldApproveReservation() {
        Reservation r = reservationService.createReservation(studentId, bookId);

        Reservation approved = reservationService.approveReservation(r.getId(), studentId);

        assertThat(approved.getStatus()).isEqualTo(ReservationStatus.ISSUED);
        assertThat(approved.getIssueDate()).isNotNull();
        assertThat(approved.getDueDate()).isNotNull();

        Book updated = bookRepository.findById(bookId).orElseThrow();
        assertThat(updated.getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void shouldRejectReservation() {
        Reservation r = reservationService.createReservation(studentId, bookId);

        Reservation rejected = reservationService.rejectReservation(r.getId(), studentId, "Book not available");

        assertThat(rejected.getStatus()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(rejected.getRejectionNote()).isEqualTo("Book not available");
    }

    @Test
    void shouldReturnReservation() {
        Reservation r = reservationService.createReservation(studentId, bookId);
        reservationService.approveReservation(r.getId(), studentId);

        Reservation returned = reservationService.returnReservation(r.getId(), studentId);

        assertThat(returned.getStatus()).isEqualTo(ReservationStatus.RETURNED);
        assertThat(returned.getReturnDate()).isNotNull();

        Book updated = bookRepository.findById(bookId).orElseThrow();
        assertThat(updated.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void shouldCalculateFineOnLateReturn() {
        // Create and approve reservation
        Reservation r = reservationService.createReservation(studentId, bookId);
        r = reservationService.approveReservation(r.getId(), studentId);

        // Manually set due date to past (simulate late return)
        r.setDueDate(java.time.LocalDate.now().minusDays(5));
        reservationRepository.save(r);

        // Return the book
        Reservation returned = reservationService.returnReservation(r.getId(), studentId);

        assertThat(returned.getStatus()).isEqualTo(ReservationStatus.RETURNED);

        // Verify fine was created
        var fineOpt = fineRepository.findByReservationIdAndStatus(r.getId(), com.ulms.model.enums.FineStatus.UNPAID);
        assertThat(fineOpt).isPresent();

        com.ulms.model.Fine fine = fineOpt.get();
        // 5 days * 0.50 (default rate) = 2.50
        assertThat(fine.getAmount()).isEqualByComparingTo("2.50");
        assertThat(fine.getReason()).contains("5 days overdue");
    }

    @Test
    void shouldLimitActiveReservations() {
        Book book2 = new Book();
        book2.setIsbn("978-RES2");
        book2.setTitle("Book2");
        book2.setAuthor("A");
        book2.setCategory("Programming");
        book2.setTotalCopies(3);
        book2.setAvailableCopies(3);
        book2 = bookRepository.save(book2);

        Book book3 = new Book();
        book3.setIsbn("978-RES3");
        book3.setTitle("Book3");
        book3.setAuthor("A");
        book3.setCategory("Programming");
        book3.setTotalCopies(3);
        book3.setAvailableCopies(3);
        book3 = bookRepository.save(book3);

        reservationService.createReservation(studentId, bookId);
        reservationService.createReservation(studentId, book2.getId());
        reservationService.createReservation(studentId, book3.getId());

        RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> reservationService.createReservation(studentId, bookId)
        );
        assertThat(thrown.getMessage()).contains("Maximum active reservations");
    }
}