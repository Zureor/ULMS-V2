package com.ulms.controller;

import com.ulms.dto.request.RegisterRequest;
import com.ulms.model.User;
import com.ulms.model.enums.UserRole;
import com.ulms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                             Model model) {
        Page<User> users = userService.getAllUsers(
                PageRequest.of(page, 20, Sort.by("id").ascending()));
        model.addAttribute("users", users);
        model.addAttribute("roles", UserRole.values());
        return "admin/users";
    }

    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("editMode", false);
        return "admin/user-form";
    }

    @GetMapping("/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        RegisterRequest req = new RegisterRequest();
        req.setUsername(user.getUsername());
        req.setFullName(user.getFullName());
        req.setEmail(user.getEmail());
        req.setRole(user.getRole().name());
        req.setStudentId(user.getStudentId());
        req.setDepartment(user.getDepartment());
        
        model.addAttribute("registerRequest", req);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("editMode", true);
        return "admin/user-form";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                              BindingResult result,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("editMode", false);
            return "admin/user-form";
        }
        try {
            userService.createUser(request);
            return "redirect:/api/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("editMode", false);
            return "admin/user-form";
        }
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                              @Valid @ModelAttribute("registerRequest") RegisterRequest request,
                              BindingResult result,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.getUserById(id));
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("editMode", true);
            return "admin/user-form";
        }
        try {
            userService.updateUser(id, request);
            return "redirect:/api/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", userService.getUserById(id));
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("editMode", true);
            return "admin/user-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/api/users";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Page<User>> getAllUsersApi(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(userService.getAllUsers(
                PageRequest.of(page, 20, Sort.by("id").ascending())));
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<User> getUserApi(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<User> createUserApi(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<User> updateUserApi(@PathVariable Long id,
                                               @RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUserApi(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}