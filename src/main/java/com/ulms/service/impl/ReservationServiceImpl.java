package com.ulms.service.impl;

import com.ulms.dto.request.ReservationRequest;
import com.ulms.model.AuditLog;
import com.ulms.model.Book;
import com.ulms.model.Reservation;
import com.ulms.model.User;
import com.ulms.model.enums.ReservationStatus;
import com.ulms.repository.AuditLogRepository;
import com.ulms.repository.BookRepository;
import com.ulms.repository.ReservationRepository;
import com.ulms.repository.UserRepository;
import com.ulms.service.ReservationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);
    private static final int MAX_ACTIVE_RESERVATIONS = 3;

    @org.springframework.beans.factory.annotation.Value("${ulms.fine.loan-period-days:14}")
    private int loanPeriodDays;

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final com.ulms.repository.FineRepository fineRepository;

    @org.springframework.beans.factory.annotation.Value("${ulms.fine.daily-rate:0.50}")
    private BigDecimal dailyFineRate;

    @org.springframework.beans.factory.annotation.Value("${ulms.fine.max-fine:50.00}")
    private BigDecimal maxFine;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                   BookRepository bookRepository,
                                   UserRepository userRepository,
                                   AuditLogRepository auditLogRepository,
                                   com.ulms.repository.FineRepository fineRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.fineRepository = fineRepository;
    }

    @Override
    @Transactional
    public Reservation createReservation(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Check for unpaid fines before allowing new reservation
        java.math.BigDecimal totalUnpaid = fineRepository.getTotalUnpaidFinesByUserId(userId);
        if (totalUnpaid.compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot create reservation. You have unpaid fines: $" + totalUnpaid);
        }

        long activeCount = reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.PENDING)
                + reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.APPROVED)
                + reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.ISSUED);
        if (activeCount >= MAX_ACTIVE_RESERVATIONS) {
            throw new RuntimeException("Maximum active reservations reached (" + MAX_ACTIVE_RESERVATIONS + ")");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available for: " + book.getTitle());
        }

        log.debug("Checking for existing pending reservation: user={}, book={}", userId, bookId);
        if (reservationRepository.findByUserIdAndBookIdAndStatus(userId, bookId, ReservationStatus.PENDING).isPresent()) {
            log.warn("Duplicate reservation attempt blocked for user {} and book {}", userId, bookId);
            throw new RuntimeException("Already have a pending reservation for this book");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.PENDING);

        // Decrement available copies immediately to hold the book
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Reservation saved = reservationRepository.save(reservation);
        audit(user, "CREATE", "Reservation", saved.getId(), "Requested book: " + book.getTitle());
        return saved;
    }

    @Override
    @Transactional
    public Reservation approveReservation(Long reservationId, Long approvedById) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Only pending reservations can be approved");
        }

        User approvedBy = userRepository.findById(approvedById)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        // Availability was already decremented at creation
        reservation.setStatus(ReservationStatus.ISSUED);
        reservation.setApprovedBy(approvedBy);
        reservation.setIssueDate(LocalDate.now());
        reservation.setDueDate(LocalDate.now().plusDays(loanPeriodDays));

        Reservation saved = reservationRepository.save(reservation);
        audit(approvedBy, "APPROVE", "Reservation", reservationId,
                "Approved reservation for user " + reservation.getUser().getUsername());
        return saved;
    }

    @Override
    @Transactional
    public Reservation rejectReservation(Long reservationId, Long rejectedById, String note) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Only pending reservations can be rejected");
        }

        User rejectedBy = userRepository.findById(rejectedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        reservation.setStatus(ReservationStatus.REJECTED);
        reservation.setRejectionNote(note);

        // Return the copy to availability
        Book book = reservation.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        Reservation saved = reservationRepository.save(reservation);
        audit(rejectedBy, "REJECT", "Reservation", reservationId, "Rejected reservation: " + note);
        return saved;
    }

    @Override
    @Transactional
    public Reservation returnReservation(Long reservationId, Long returnedById) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation.getStatus() != ReservationStatus.ISSUED) {
            throw new RuntimeException("Only issued reservations can be returned");
        }

        User returnedBy = userRepository.findById(returnedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.setReturnDate(today);

        // Calculate fine if overdue
        if (reservation.getDueDate() != null && today.isAfter(reservation.getDueDate())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(reservation.getDueDate(), today);
            calculateAndApplyFine(reservation, daysOverdue);
        }

        Book book = reservation.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        Reservation saved = reservationRepository.save(reservation);
        audit(returnedBy, "RETURN", "Reservation", reservationId,
                "Returned book: " + book.getTitle());
        return saved;
    }

    private void calculateAndApplyFine(Reservation reservation, long daysOverdue) {
        java.math.BigDecimal amount = dailyFineRate.multiply(java.math.BigDecimal.valueOf(daysOverdue));
        if (amount.compareTo(maxFine) > 0) {
            amount = maxFine;
        }

        // Check if there's already an existing unpaid fine for this reservation (from scheduler)
        var existingFineOpt = fineRepository.findByReservationIdAndStatus(reservation.getId(), com.ulms.model.enums.FineStatus.UNPAID);

        if (existingFineOpt.isPresent()) {
            var existingFine = existingFineOpt.get();
            // If the final return-time fine is higher than the last scheduled one, update it
            if (amount.compareTo(existingFine.getAmount()) > 0) {
                existingFine.setAmount(amount);
                existingFine.setReason("LATE_RETURN (" + daysOverdue + " days overdue)");
                fineRepository.save(existingFine);
                log.info("Updated existing fine for reservation {} to {}", reservation.getId(), amount);
            }
        } else {
            // Create new fine
            com.ulms.model.Fine fine = new com.ulms.model.Fine();
            fine.setUser(reservation.getUser());
            fine.setReservation(reservation);
            fine.setAmount(amount);
            fine.setReason("LATE_RETURN (" + daysOverdue + " days overdue)");
            fine.setStatus(com.ulms.model.enums.FineStatus.UNPAID);
            fineRepository.save(fine);
            log.info("Created new fine for reservation {} amount {}", reservation.getId(), amount);
        }
    }

    @Override
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByFilters(userId, null, null);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findByFilters(null, null, null);
    }

    @Override
    public List<Reservation> getPendingReservations() {
        return reservationRepository.findByFilters(null, ReservationStatus.PENDING, null);
    }

    private Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
    }

    private void audit(User user, String action, String entityType, Long entityId, String details) {
        if (user == null) return;
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setPerformedBy(user);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}