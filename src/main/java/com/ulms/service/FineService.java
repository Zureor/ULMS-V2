package com.ulms.service;

import com.ulms.dto.response.FineResponse;
import com.ulms.model.Fine;
import com.ulms.model.enums.FineStatus;

import java.math.BigDecimal;
import java.util.List;

public interface FineService {
    List<FineResponse> getFinesByUser(Long userId);
    BigDecimal getTotalUnpaidFines(Long userId);
    List<FineResponse> getAllFines();
    Fine payFine(Long fineId, Long paidById);
    Fine waiveFine(Long fineId, Long waivedById);
}