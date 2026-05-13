package com.ulms.repository;

import com.ulms.model.Reservation;
import com.ulms.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);
    List<Reservation> findByBookIdAndStatus(Long bookId, ReservationStatus status);
    Optional<Reservation> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE " +
           "(:userId IS NULL OR r.user.id = :userId) AND " +
           "(:status IS NULL OR r.status = :status)")
    List<Reservation> findByFilters(@Param("userId") Long userId,
                                    @Param("status") ReservationStatus status,
                                    org.springframework.data.domain.Pageable pageable);

    long countByUserIdAndStatus(Long userId, ReservationStatus status);

    long countByStatus(ReservationStatus status);

    @Query("SELECT r.id FROM Reservation r WHERE r.status = 'ISSUED' AND r.dueDate < :today AND r.returnDate IS NULL")
    List<Long> findOverdueReservations(@Param("today") LocalDate today);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Fine f WHERE f.reservation.id = :reservationId AND f.status = 'UNPAID'")
    boolean existsByReservationIdAndStatus(@Param("reservationId") Long reservationId, @Param("status") String status);
}