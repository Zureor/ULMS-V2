package com.ulms.dto.request;

import jakarta.validation.constraints.NotNull;

public class FinePaymentRequest {

    @NotNull(message = "Fine ID is required")
    private Long fineId;

    public FinePaymentRequest() {}

    public Long getFineId() { return fineId; }
    public void setFineId(Long fineId) { this.fineId = fineId; }
}