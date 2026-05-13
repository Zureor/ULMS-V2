package com.ulms.service;

import com.ulms.model.Reservation;
import com.ulms.model.enums.ReservationStatus;

import java.util.List;

public interface ReservationService {
    Reservation createReservation(Long userId, Long bookId);
    Reservation approveReservation(Long reservationId, Long approvedById);
    Reservation rejectReservation(Long reservationId, Long rejectedById, String note);
    Reservation returnReservation(Long reservationId, Long returnedById);
    List<Reservation> getUserReservations(Long userId);
    List<Reservation> getAllReservations();
    List<Reservation> getPendingReservations();
}