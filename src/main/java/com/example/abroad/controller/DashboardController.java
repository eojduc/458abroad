package com.example.abroad.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;


@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(HttpServletRequest request, Model model) {
        String username = (String) request.getSession().getAttribute("username");
        System.out.println("Getting username from session: " + request.getSession().getAttribute("username"));
        model.addAttribute("username", username);
        model.addAttribute("student", username); // Add this for navbar
        return "dashboard/student-dashboard :: page";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpServletRequest request, Model model) {
        var username = Optional.ofNullable(request.getSession().getAttribute("username"))
                .filter(obj -> obj instanceof String)
                .map(obj -> (String) obj)
                .orElse("admin"); // Default fallback

        model.addAttribute("username", username);
        return "dashboard/admin-dashboard :: page";  // Note the :: page suffix
    }

    @GetMapping("/hello")
    public String hello(Authentication authentication) {
        if (authentication != null) {
            System.out.println("User is authenticated as: " + authentication.getName());
        } else {
            System.out.println("No authentication found");
        }
        return "hello";
    }

    @GetMapping("/test-auth")
    public String testAuth(Authentication authentication, Model model) {
        System.out.println("Test Auth endpoint called");
        if (authentication != null) {
            System.out.println("User is authenticated as: " + authentication.getName());
            model.addAttribute("username", authentication.getName());
            model.addAttribute("roles", authentication.getAuthorities());
        } else {
            System.out.println("No authentication found in /test-auth");
        }
        return "test-auth";
    }
}