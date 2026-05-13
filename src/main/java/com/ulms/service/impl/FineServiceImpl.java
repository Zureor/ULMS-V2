package com.ulms.service.impl;

import com.ulms.dto.response.FineResponse;
import com.ulms.model.AuditLog;
import com.ulms.model.Fine;
import com.ulms.model.User;
import com.ulms.model.enums.FineStatus;
import com.ulms.repository.AuditLogRepository;
import com.ulms.repository.FineRepository;
import com.ulms.repository.UserRepository;
import com.ulms.service.FineService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FineServiceImpl implements FineService {

    private static final Logger log = LoggerFactory.getLogger(FineServiceImpl.class);

    private final FineRepository fineRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public FineServiceImpl(FineRepository fineRepository,
                           UserRepository userRepository,
                           AuditLogRepository auditLogRepository) {
        this.fineRepository = fineRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<FineResponse> getFinesByUser(Long userId) {
        return fineRepository.findByUserIdAndStatus(userId, FineStatus.UNPAID)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalUnpaidFines(Long userId) {
        return fineRepository.getTotalUnpaidFinesByUserId(userId);
    }

    @Override
    public List<FineResponse> getAllFines() {
        return fineRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Fine payFine(Long fineId, Long paidById) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found with id: " + fineId));

        if (fine.getStatus() != FineStatus.UNPAID) {
            throw new RuntimeException("Fine is already " + fine.getStatus());
        }

        fine.setStatus(FineStatus.PAID);
        fine.setPaidAt(LocalDateTime.now());

        User paidBy = userRepository.findById(paidById).orElse(null);
        Fine saved = fineRepository.save(fine);

        audit(paidBy, "PAY", "Fine", fineId, "Paid fine #" + fineId + " amount: " + fine.getAmount());
        return saved;
    }

    @Override
    @Transactional
    public Fine waiveFine(Long fineId, Long waivedById) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found with id: " + fineId));

        fine.setStatus(FineStatus.WAIVED);

        User waivedBy = userRepository.findById(waivedById).orElse(null);
        Fine saved = fineRepository.save(fine);

        audit(waivedBy, "WAIVE", "Fine", fineId, "Waived fine #" + fineId);
        return saved;
    }

    private FineResponse toResponse(Fine fine) {
        return new FineResponse(
                fine.getId(),
                fine.getUser().getId(),
                fine.getUser().getFullName(),
                fine.getReservation().getId(),
                fine.getReservation().getBook().getTitle(),
                fine.getAmount(),
                fine.getReason(),
                fine.getStatus().name(),
                fine.getPaidAt(),
                fine.getCreatedAt()
        );
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