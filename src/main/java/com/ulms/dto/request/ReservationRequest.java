package com.ulms.dto.request;

import jakarta.validation.constraints.NotNull;

public class ReservationRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    public ReservationRequest() {}

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
}