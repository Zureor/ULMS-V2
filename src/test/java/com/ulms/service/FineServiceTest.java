package com.ulms.service;

import com.ulms.model.Book;
import com.ulms.model.Fine;
import com.ulms.model.Reservation;
import com.ulms.model.User;
import com.ulms.model.enums.FineStatus;
import com.ulms.model.enums.ReservationStatus;
import com.ulms.model.enums.UserRole;
import com.ulms.repository.BookRepository;
import com.ulms.repository.FineRepository;
import com.ulms.repository.ReservationRepository;
import com.ulms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class FineServiceTest {

    @Autowired
    private FineService fineService;

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long studentId;

    @BeforeEach
    void setUp() {
        User student = new User();
        student.setUsername("fine_student");
        student.setPassword("encoded");
        student.setFullName("Fine Student");
        student.setEmail("fs@test.com");
        student.setRole(UserRole.STUDENT);
        student.setStudentId("STU888");
        student.setActive(true);
        student = userRepository.save(student);
        studentId = student.getId();
    }

    @Test
    void shouldGetUnpaidFines() {
        Book book = new Book();
        book.setIsbn("978-FINE");
        book.setTitle("Fine Book");
        book.setAuthor("A");
        book.setCategory("Programming");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
        book = bookRepository.save(book);

        Reservation r = new Reservation();
        User userRef = new User();
        userRef.setId(studentId);
        r.setUser(userRef);
        r.setBook(book);
        r.setStatus(ReservationStatus.ISSUED);
        r.setIssueDate(LocalDate.now().minusDays(10));
        r.setDueDate(LocalDate.now().minusDays(5));
        r = reservationRepository.save(r);

        Fine fine = new Fine();
        fine.setUser(userRef);
        fine.setReservation(r);
        fine.setAmount(new BigDecimal("2.50"));
        fine.setReason("OVERDUE_5_DAYS");
        fine.setStatus(FineStatus.UNPAID);
        fineRepository.save(fine);

        BigDecimal total = fineService.getTotalUnpaidFines(studentId);
        assertThat(total).isNotNull();
    }
}