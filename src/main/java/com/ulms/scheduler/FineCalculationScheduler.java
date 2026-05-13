package com.ulms.scheduler;

import com.ulms.repository.FineRepository;
import com.ulms.repository.ReservationRepository;
import com.ulms.model.enums.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Component
public class FineCalculationScheduler {

    private static final Logger log = LoggerFactory.getLogger(FineCalculationScheduler.class);

    @org.springframework.beans.factory.annotation.Value("${ulms.fine.daily-rate:0.50}")
    private BigDecimal dailyFineRate;

    @org.springframework.beans.factory.annotation.Value("${ulms.fine.max-fine:50.00}")
    private BigDecimal maxFine;

    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;

    public FineCalculationScheduler(ReservationRepository reservationRepository,
                                     FineRepository fineRepository) {
        this.reservationRepository = reservationRepository;
        this.fineRepository = fineRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Runs at 2 AM every day
    @Transactional
    public void calculateOverdueFines() {
        LocalDate today = LocalDate.now();
        log.info("Running daily overdue fine calculation for: {}", today);

        List<Long> overdueReservationIds = reservationRepository.findOverdueReservations(today);
        log.info("Found {} active overdue reservations", overdueReservationIds.size());

        int finesUpdated = 0;
        int finesCreated = 0;

        for (Long reservationId : overdueReservationIds) {
            var reservation = reservationRepository.findById(reservationId).orElse(null);
            if (reservation == null) continue;

            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(reservation.getDueDate(), today);
            if (daysOverdue <= 0) continue;

            BigDecimal totalCalculatedFine = dailyFineRate.multiply(BigDecimal.valueOf(daysOverdue));
            if (totalCalculatedFine.compareTo(maxFine) > 0) {
                totalCalculatedFine = maxFine;
            }

            // Check if there's already an unpaid fine for this specific overdue reservation
            var existingFineOpt = fineRepository.findByReservationIdAndStatus(reservationId, com.ulms.model.enums.FineStatus.UNPAID);

            if (existingFineOpt.isPresent()) {
                // Update existing fine amount
                var existingFine = existingFineOpt.get();
                if (existingFine.getAmount().compareTo(totalCalculatedFine) < 0) {
                    existingFine.setAmount(totalCalculatedFine);
                    existingFine.setReason("LATE_RETURN (" + daysOverdue + " days overdue)");
                    fineRepository.save(existingFine);
                    finesUpdated++;
                }
            } else {
                // Create new fine entry
                com.ulms.model.Fine fine = new com.ulms.model.Fine();
                fine.setUser(reservation.getUser());
                fine.setReservation(reservation);
                fine.setAmount(totalCalculatedFine);
                fine.setReason("LATE_RETURN (" + daysOverdue + " days overdue)");
                fine.setStatus(com.ulms.model.enums.FineStatus.UNPAID);
                fineRepository.save(fine);
                finesCreated++;
            }
        }

        log.info("Fine calculation complete. Created: {}, Updated: {}", finesCreated, finesUpdated);
    }
}