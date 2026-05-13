package com.ulms.controller;

import com.ulms.dto.request.FinePaymentRequest;
import com.ulms.dto.response.FineResponse;
import com.ulms.model.User;
import com.ulms.service.FineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public String listFines(Authentication auth, Model model) {
        User user = (User) auth.getPrincipal();
        model.addAttribute("role", user.getRole().name());

        List<FineResponse> fines;
        BigDecimal total = BigDecimal.ZERO;

        if ("STUDENT".equals(user.getRole().name())) {
            fines = fineService.getFinesByUser(user.getId());
            total = fineService.getTotalUnpaidFines(user.getId());
        } else {
            fines = fineService.getAllFines();
        }

        model.addAttribute("fines", fines);
        model.addAttribute("totalUnpaid", total);
        return "STUDENT".equals(user.getRole().name()) ? "student/fines" : "admin/fines";
    }

    @PostMapping("/{id}/pay")
    public String payFine(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        fineService.payFine(id, user.getId());
        return "redirect:/api/fines";
    }

    @PostMapping("/{id}/waive")
    public String waiveFine(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        fineService.waiveFine(id, user.getId());
        return "redirect:/api/fines";
    }

    @GetMapping("/api/my")
    @ResponseBody
    public ResponseEntity<List<FineResponse>> getMyFines(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(fineService.getFinesByUser(user.getId()));
    }

    @PutMapping("/api/{id}/pay")
    @ResponseBody
    public ResponseEntity<?> payFineApi(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        fineService.payFine(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Fine paid", "id", id));
    }

    @DeleteMapping("/api/{id}/waive")
    @ResponseBody
    public ResponseEntity<?> waiveFineApi(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        fineService.waiveFine(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Fine waived", "id", id));
    }
}