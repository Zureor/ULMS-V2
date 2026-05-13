package com.ulms.repository;

import com.ulms.model.Fine;
import com.ulms.model.enums.FineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserIdAndStatus(Long userId, FineStatus status);

    Optional<Fine> findByReservationIdAndStatus(Long reservationId, FineStatus status);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.user.id = :userId AND f.status = 'UNPAID'")
    BigDecimal getTotalUnpaidFinesByUserId(@Param("userId") Long userId);

    List<Fine> findByReservationId(Long reservationId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Fine f WHERE f.reservation.id = :reservationId AND f.status = 'UNPAID'")
    boolean existsByReservationIdAndStatus(@Param("reservationId") Long reservationId, @Param("status") String status);

    @Query("SELECT f FROM Fine f WHERE f.user.id = :userId AND f.status = :status")
    Page<Fine> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FineStatus status, Pageable pageable);
}