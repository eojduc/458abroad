package com.example.abroad.controller;

import com.example.abroad.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("isAdmin", userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        return "dashboard";
    }
}