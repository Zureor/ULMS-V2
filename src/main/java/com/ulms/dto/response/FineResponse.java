package com.ulms.dto.response;

import java.math.BigDecimal;

public class FineResponse {

    private Long id;
    private Long userId;
    private String studentName;
    private Long reservationId;
    private String bookTitle;
    private BigDecimal amount;
    private String reason;
    private String status;
    private String paidAt;
    private String createdAt;

    public FineResponse() {}

    public FineResponse(Long id, Long userId, String studentName, Long reservationId, String bookTitle,
                        BigDecimal amount, String reason, String status,
                        java.time.LocalDateTime paidAt, java.time.LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.studentName = studentName;
        this.reservationId = reservationId;
        this.bookTitle = bookTitle;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.paidAt = paidAt != null ? paidAt.toString() : null;
        this.createdAt = createdAt != null ? createdAt.toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}