package com.ulms.controller;

import com.ulms.dto.request.LoginRequest;
import com.ulms.dto.response.AuthResponse;
import com.ulms.model.User;
import com.ulms.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request,
                         BindingResult result,
                         HttpSession session) {
        if (result.hasErrors()) return "auth/login";
        AuthResponse auth = authService.login(request);
        session.setAttribute("currentUser", auth);
        session.setAttribute("token", auth.getToken());
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login?logout";
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiLogin(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}