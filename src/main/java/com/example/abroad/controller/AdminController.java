package com.example.abroad.controller;

import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.User;
import com.example.abroad.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")  // Ensures all endpoints require admin role
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        return "admin/dashboard";
    }

    @GetMapping("/create")
    public String showAdminCreationForm() {
        return "admin/create";
    }

    @PostMapping("/create")
    public String createAdmin(@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String password,
                              HttpServletRequest request,
                              Model model) {
        try {
            User creator = userService.getUser(request)
                    .orElseThrow(() -> new IllegalStateException("No authenticated user found"));

            userService.createAdmin(username, email, password, creator);
            return "redirect:/admin/dashboard?adminCreated=true";
        } catch (UsernameAlreadyInUseException e) {
            model.addAttribute("error", "Username is already taken");
            return "admin/create";
        } catch (EmailAlreadyInUseException e) {
            model.addAttribute("error", "Email is already registered");
            return "admin/create";
        }
    }
}