package com.ulms.controller;

import com.ulms.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final com.ulms.repository.UserRepository userRepository;
    private final com.ulms.repository.BookRepository bookRepository;
    private final com.ulms.repository.ReservationRepository reservationRepository;
    private final com.ulms.service.FineService fineService;

    public DashboardController(com.ulms.repository.UserRepository userRepository,
                               com.ulms.repository.BookRepository bookRepository,
                               com.ulms.repository.ReservationRepository reservationRepository,
                               com.ulms.service.FineService fineService) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.reservationRepository = reservationRepository;
        this.fineService = fineService;
    }

    @GetMapping
    public String dashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();
        String role = user.getRole().name();
        
        if ("ADMIN".equals(role)) {
            return "redirect:/dashboard/admin";
        } else if ("LIBRARIAN".equals(role)) {
            return "redirect:/dashboard/librarian";
        } else {
            return "redirect:/dashboard/student";
        }
    }

    @GetMapping("/admin")
    public String adminDashboard(org.springframework.ui.Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalBooks", bookRepository.count());
        model.addAttribute("pendingReservations", reservationRepository.countByStatus(com.ulms.model.enums.ReservationStatus.PENDING));
        model.addAttribute("activeIssues", reservationRepository.countByStatus(com.ulms.model.enums.ReservationStatus.ISSUED));
        return "admin/dashboard";
    }

    @GetMapping("/librarian")
    public String librarianDashboard(org.springframework.ui.Model model) {
        model.addAttribute("totalBooks", bookRepository.count());
        model.addAttribute("pendingReservations", reservationRepository.countByStatus(com.ulms.model.enums.ReservationStatus.PENDING));
        model.addAttribute("activeIssues", reservationRepository.countByStatus(com.ulms.model.enums.ReservationStatus.ISSUED));
        return "librarian/dashboard";
    }

    @GetMapping("/student")
    public String studentDashboard(Authentication auth, org.springframework.ui.Model model) {
        User user = (User) auth.getPrincipal();
        model.addAttribute("pendingCount", reservationRepository.countByUserIdAndStatus(user.getId(), com.ulms.model.enums.ReservationStatus.PENDING));
        model.addAttribute("issuedCount", reservationRepository.countByUserIdAndStatus(user.getId(), com.ulms.model.enums.ReservationStatus.ISSUED));
        model.addAttribute("unpaidFines", fineService.getTotalUnpaidFines(user.getId()));
        return "student/dashboard";
    }
}
