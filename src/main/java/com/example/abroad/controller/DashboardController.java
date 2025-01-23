package com.example.abroad.controller;

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
}