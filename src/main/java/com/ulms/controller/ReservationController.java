package com.ulms.controller;

import com.ulms.dto.request.ReservationRequest;
import com.ulms.model.Reservation;
import com.ulms.model.User;
import com.ulms.model.enums.ReservationStatus;
import com.ulms.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String listReservations(@RequestParam(required = false) String status,
                                    Authentication auth, Model model) {
        User user = (User) auth.getPrincipal();
        model.addAttribute("role", user.getRole().name());

        ReservationStatus rs = null;
        if (status != null && !status.isEmpty()) {
            try { rs = ReservationStatus.valueOf(status.toUpperCase()); } catch (Exception e) {}
        }

        final ReservationStatus filterStatus = rs;
        List<Reservation> reservations;
        if ("STUDENT".equals(user.getRole().name())) {
            reservations = reservationService.getUserReservations(user.getId());
        } else if (filterStatus != null) {
            reservations = reservationService.getAllReservations()
                    .stream().filter(r -> r.getStatus() == filterStatus).toList();
        } else {
            reservations = reservationService.getAllReservations();
        }

        model.addAttribute("reservations", reservations);
        model.addAttribute("statusFilter", status);
        return "STUDENT".equals(user.getRole().name()) ? "student/reservations" : "admin/reservations";
    }

    @PostMapping
    public String createReservation(@Valid @ModelAttribute ReservationRequest request,
                                     BindingResult result, Authentication auth,
                                     org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "redirect:/api/books";
        }
        User user = (User) auth.getPrincipal();
        try {
            reservationService.createReservation(user.getId(), request.getBookId());
            redirectAttributes.addFlashAttribute("success", "Reservation requested successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/api/books";
        }
        return "redirect:/api/reservations";
    }

    @PostMapping("/{id}/approve")
    public String approveReservation(@PathVariable Long id, Authentication auth,
                                     org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = (User) auth.getPrincipal();
        try {
            reservationService.approveReservation(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Reservation approved and book issued!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/api/reservations";
    }

    @PostMapping("/{id}/reject")
    public String rejectReservation(@PathVariable Long id,
                                     @RequestParam String note,
                                     Authentication auth,
                                     org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = (User) auth.getPrincipal();
        try {
            reservationService.rejectReservation(id, user.getId(), note);
            redirectAttributes.addFlashAttribute("success", "Reservation rejected.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/api/reservations";
    }

    @PostMapping("/{id}/return")
    public String returnReservation(@PathVariable Long id, Authentication auth,
                                     org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = (User) auth.getPrincipal();
        try {
            reservationService.returnReservation(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Book returned successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/api/reservations";
    }

    @GetMapping("/api/my")
    @ResponseBody
    public ResponseEntity<List<Reservation>> getMyReservations(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(reservationService.getUserReservations(user.getId()));
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createReservationApi(@RequestBody @Valid ReservationRequest request,
                                                    Authentication auth) {
        User user = (User) auth.getPrincipal();
        Reservation reservation = reservationService.createReservation(user.getId(), request.getBookId());
        return ResponseEntity.ok(Map.of("message", "Reservation created", "id", reservation.getId()));
    }

    @PutMapping("/api/{id}/approve")
    @ResponseBody
    public ResponseEntity<?> approveReservationApi(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Reservation r = reservationService.approveReservation(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Approved", "id", r.getId()));
    }

    @PutMapping("/api/{id}/reject")
    @ResponseBody
    public ResponseEntity<?> rejectReservationApi(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body,
                                                    Authentication auth) {
        User user = (User) auth.getPrincipal();
        Reservation r = reservationService.rejectReservation(id, user.getId(), body.get("note"));
        return ResponseEntity.ok(Map.of("message", "Rejected", "id", r.getId()));
    }

    @PutMapping("/api/{id}/return")
    @ResponseBody
    public ResponseEntity<?> returnReservationApi(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Reservation r = reservationService.returnReservation(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Returned", "id", r.getId()));
    }
}